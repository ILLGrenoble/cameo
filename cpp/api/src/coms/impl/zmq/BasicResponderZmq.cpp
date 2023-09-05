/*
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under the EUPL, Version 1.1 only (the "License");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */

#include "BasicResponderZmq.h"
#include "Application.h"
#include "Messages.h"
#include "JSON.h"
#include "BasicResponder.h"
#include "RequestSocket.h"
#include "ContextZmq.h"

namespace cameo {
namespace coms {
namespace basic {

ResponderZmq::ResponderZmq() :
	m_responderPort{0},
	m_canceled{false} {
}

ResponderZmq::~ResponderZmq() {
	terminate();
}

void ResponderZmq::init(const std::string& responderIdentity) {

	m_responderIdentity = responderIdentity;

	// Create a socket ROUTER.
	ContextZmq * contextImpl {dynamic_cast<ContextZmq *>(This::getCom().getContext())};
	m_responder.reset(new zmq::socket_t{contextImpl->getContext(), zmq::socket_type::router});

	// Set the identity.
	m_responder->setsockopt(ZMQ_IDENTITY, responderIdentity.data(), responderIdentity.size());

	// Connect to the proxy.
	Endpoint proxyEndpoint {This::getEndpoint().withPort(This::getCom().getResponderProxyPort())};
	m_responder->connect(proxyEndpoint.toString());

	std::string endpointPrefix {"tcp://*:"};

	// Loop to find an available port for the responder.
	while (true) {

		int port {This::getCom().requestPort()};
		std::string repEndpoint {endpointPrefix + std::to_string(port)};

		try {
			m_responder->bind(repEndpoint.c_str());
			m_responderPort = port;
			break;
		}
		catch (...) {
			This::getCom().setPortUnavailable(port);
		}
	}
}

int ResponderZmq::getResponderPort() {
	return m_responderPort;
}

void ResponderZmq::cancel() {

	if (m_canceled) {
		return;
	}

	json::StringObject jsonRequest;
	jsonRequest.pushKey(message::TYPE);
	jsonRequest.pushValue(message::CANCEL);

	// Create a request socket connected directly to the responder.
	std::unique_ptr<RequestSocket> requestSocket {This::getCom().createRequestSocket(This::getEndpoint().withPort(m_responderPort).toString(), m_responderIdentity)};
	requestSocket->requestJSON(jsonRequest.dump());
}

bool ResponderZmq::isCanceled() {
	return m_canceled;
}

std::unique_ptr<Request> ResponderZmq::receive() {

	m_proxyIdentity.reset(new zmq::message_t{});
	m_requesterIdentity.reset(new zmq::message_t{});

	while (true) {

		// Get the identity of the proxy which can be empty when no proxy is used.
		if (!m_responder->recv(*m_proxyIdentity, zmq::recv_flags::none).has_value()) {
			return {};
		}

		// Followed by an empty message.
		zmq::message_t empty;
		if (!m_responder->recv(empty, zmq::recv_flags::none).has_value()) {
			return {};
		}

		// Get the identity of the requester.
		if (!m_responder->recv(*m_requesterIdentity, zmq::recv_flags::none).has_value()) {
			return {};
		}

		// Followed by an empty message.
		if (!m_responder->recv(empty, zmq::recv_flags::none).has_value()) {
			return {};
		}

		// Get the request part.
		zmq::message_t requestPart;
		if (!m_responder->recv(requestPart, zmq::recv_flags::none).has_value()) {
			return {};
		}

		// Get the JSON request.
		json::Object jsonRequest;
		json::parse(jsonRequest, requestPart);

		int type = jsonRequest[message::TYPE].GetInt();

		// Create the reply.
		std::unique_ptr<zmq::message_t> reply;

		if (type == message::REQUEST) {

			std::string name {jsonRequest[message::Request::APPLICATION_NAME].GetString()};
			int id {jsonRequest[message::Request::APPLICATION_ID].GetInt()};
			std::string serverEndpoint {jsonRequest[message::Request::SERVER_ENDPOINT].GetString()};
			int serverProxyPort {jsonRequest[message::Request::SERVER_PROXY_PORT].GetInt()};

			// Get the second part for the message.
			zmq::message_t secondPart;
			if (!m_responder->recv(secondPart, zmq::recv_flags::none).has_value()) {
				return {};
			}
			std::string message1 {secondPart.data<char>(), secondPart.size()};
			std::string message2;

			// Set message 2 if it exists.
			if (secondPart.more()) {
				zmq::message_t thirdPart;
				if (!m_responder->recv(thirdPart, zmq::recv_flags::none).has_value()) {
					return {};
				}
				message2 = std::string(thirdPart.data<char>(), thirdPart.size());
			}

			// Create the request.
			return std::unique_ptr<Request>{new Request{name,
					id,
					serverEndpoint,
					serverProxyPort,
					message1,
					message2}};
		}
		else if (type == message::CANCEL) {
			m_canceled = true;

			// Reply immediately.
			zmq::message_t empty;
			m_responder->send(*m_proxyIdentity, zmq::send_flags::sndmore);
			m_responder->send(empty, zmq::send_flags::sndmore);
			m_responder->send(*m_requesterIdentity, zmq::send_flags::sndmore);
			m_responder->send(empty, zmq::send_flags::sndmore);
			reply.reset(responseToCancelResponder());
			m_responder->send(*reply, zmq::send_flags::none);

			return {};
		}
		else if (type == message::SYNC) {

			// Reply immediately.
			zmq::message_t empty;
			m_responder->send(*m_proxyIdentity, zmq::send_flags::sndmore);
			m_responder->send(empty, zmq::send_flags::sndmore);
			m_responder->send(*m_requesterIdentity, zmq::send_flags::sndmore);
			m_responder->send(empty, zmq::send_flags::sndmore);
			reply.reset(responseToRequest());
			m_responder->send(*reply, zmq::send_flags::none);

			// Do not return, continue the loop.
		}
	}
}

void ResponderZmq::reply(const std::string& responsePart1, const std::string& responsePart2) {

	// Send the identities.
	zmq::message_t empty;
	m_responder->send(*m_proxyIdentity, zmq::send_flags::sndmore);
	m_responder->send(empty, zmq::send_flags::sndmore);
	m_responder->send(*m_requesterIdentity, zmq::send_flags::sndmore);
	m_responder->send(empty, zmq::send_flags::sndmore);

	// Send the response in two parts.
	zmq::message_t responsePart1Part {responsePart1.c_str(), responsePart1.size()};
	m_responder->send(responsePart1Part, zmq::send_flags::sndmore);

	zmq::message_t responsePart2Part {responsePart2.c_str(), responsePart2.size()};
	m_responder->send(responsePart2Part, zmq::send_flags::none);
}

zmq::message_t * ResponderZmq::responseToRequest() {

	std::string result {createRequestResponse(0, "OK")};

	return new zmq::message_t{result.c_str(), result.size()};
}

zmq::message_t * ResponderZmq::responseToCancelResponder() {

	std::string result {createRequestResponse(0, "OK")};

	return new zmq::message_t{result.c_str(), result.size()};
}

void ResponderZmq::terminate() {

	if (m_responder) {
		m_responder.reset();

		// Release the responder port.
		This::getCom().releasePort(m_responderPort);
	}
}

}
}
}

