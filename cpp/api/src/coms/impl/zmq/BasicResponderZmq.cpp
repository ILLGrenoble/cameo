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
#include "Serializer.h"
#include "JSON.h"
#include "../../../base/impl/zmq/ContextZmq.h"
#include "../../../base/Messages.h"
#include "../../../base/RequestSocket.h"
#include "BasicRequesterResponder.h"
#include <sstream>

namespace cameo {
namespace coms {
namespace basic {

ResponderZmq::ResponderZmq() :
	m_responderPort(0),
	m_canceled(false) {
}

ResponderZmq::~ResponderZmq() {
	terminate();
}

void ResponderZmq::init() {

	// Create a socket REP.
	ContextZmq* contextImpl = dynamic_cast<ContextZmq *>(application::This::getCom().getContext());
	m_responder.reset(new zmq::socket_t(contextImpl->getContext(), zmq::socket_type::rep));

	std::string endpointPrefix("tcp://*:");

	// Loop to find an available port for the responder.
	while (true) {

		int port = application::This::getCom().requestPort();
		std::string repEndpoint = endpointPrefix + std::to_string(port);

		try {
			m_responder->bind(repEndpoint.c_str());
			m_responderPort = port;
			break;
		}
		catch (...) {
			application::This::getCom().setPortUnavailable(port);
		}
	}
}

int ResponderZmq::getResponderPort() {
	return m_responderPort;
}

void ResponderZmq::cancel() {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushValue(message::CANCEL);

	// Create a request socket.
	std::unique_ptr<RequestSocket> requestSocket = application::This::getCom().createRequestSocket(application::This::getEndpoint().withPort(m_responderPort).toString());
	requestSocket->requestJSON(request.toString());
}

bool ResponderZmq::isCanceled() {
	return m_canceled;
}

std::unique_ptr<Request> ResponderZmq::receive() {

	zmq::message_t message;
	if (!m_responder->recv(message, zmq::recv_flags::none).has_value()) {
		return {};
	}

	// Get the JSON request.
	json::Object request;
	json::parse(request, message);

	int type = request[message::TYPE].GetInt();

	// Create the reply.
	std::unique_ptr<zmq::message_t> reply;
	std::unique_ptr<Request> result;

	if (type == message::REQUEST) {

		std::string name = request[message::Request::APPLICATION_NAME].GetString();
		int id = request[message::Request::APPLICATION_ID].GetInt();
		std::string serverUrl = request[message::Request::SERVER_URL].GetString();
		int serverPort = request[message::Request::SERVER_PORT].GetInt();

		// Get the second part for the message.
		zmq::message_t secondPart;
		if (!m_responder->recv(secondPart, zmq::recv_flags::none).has_value()) {
			return {};
		}
		std::string message1(secondPart.data<char>(), secondPart.size());
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
		result = std::unique_ptr<Request>(new Request(name,
				id,
				serverUrl,
				serverPort,
				message1,
				message2));
	}
	else if (type == message::CANCEL) {
		m_canceled = true;

		// Reply immediately.
		reply.reset(responseToCancelResponder());
		m_responder->send(*reply.get(), zmq::send_flags::none);
	}
	else {
		// Reply immediately.
		reply.reset(responseToUnknownRequest());
		m_responder->send(*reply.get(), zmq::send_flags::none);
	}

	// Do not reply to the client now. This will be done by the Request.
	// Return the Request object.
	return result;
}

void ResponderZmq::reply(const std::string& responsePart1, const std::string& responsePart2) {

	// Prepare the response parts.
	int responsePart1Size = responsePart1.length();
	int responsePart2Size = responsePart2.length();
	zmq::message_t responsePart1Message(responsePart1Size);
	zmq::message_t responsePart2Message(responsePart2Size);
	memcpy(static_cast<void *>(responsePart1Message.data()), responsePart1.c_str(), responsePart1Size);
	memcpy(static_cast<void *>(responsePart2Message.data()), responsePart2.c_str(), responsePart2Size);

	// Send the response in two parts.
	m_responder->send(responsePart1Message, zmq::send_flags::sndmore);
	m_responder->send(responsePart2Message, zmq::send_flags::none);
}

zmq::message_t * ResponderZmq::responseToRequest() {

	std::string result = createRequestResponse(0, "OK");

	zmq::message_t * reply = new zmq::message_t(result.length());
	memcpy(reply->data(), result.c_str(), result.length());

	return reply;
}

zmq::message_t * ResponderZmq::responseToCancelResponder() {

	std::string result = createRequestResponse(0, "OK");

	zmq::message_t * reply = new zmq::message_t(result.length());
	memcpy(reply->data(), result.c_str(), result.length());

	return reply;
}

zmq::message_t * ResponderZmq::responseToUnknownRequest() {

	std::string result = createRequestResponse(-1, "Unknown request");

	zmq::message_t * reply = new zmq::message_t(result.length());
	memcpy(reply->data(), result.c_str(), result.length());

	return reply;
}

void ResponderZmq::terminate() {

	if (m_responder.get() != nullptr) {
		m_responder.reset(nullptr);

		// Release the responder port.
		application::This::getCom().releasePort(m_responderPort);
	}
}

}
}
}

