/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

#include "BasicResponderZmq.h"

#include "This.h"
#include "Messages.h"
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
	m_responder->set(zmq::sockopt::routing_id, responderIdentity);

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
	requestSocket->request(jsonRequest.dump());
}

bool ResponderZmq::isCanceled() {
	return m_canceled;
}

std::unique_ptr<Request> ResponderZmq::processCancel() {

	m_canceled = true;

	// Reply immediately.
	replyOK();

	return {};
}

std::unique_ptr<Request> ResponderZmq::processRequest(const json::Object& jsonRequest) {

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

std::unique_ptr<Request> ResponderZmq::receive() {

	while (true) {

		// Get the headers.
		for (int i = 0; i < HEADER_SIZE; ++i) {

			zmq::message_t messagePart;
			if (!m_responder->recv(messagePart, zmq::recv_flags::none).has_value()) {
				return {};
			}

			m_requestHeader[i] = std::string{messagePart.data<char>(), messagePart.size()};
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
			return processRequest(jsonRequest);
		}
		else if (type == message::CANCEL) {
			return processCancel();
		}
		else if (type == message::SYNC) {

			// Reply immediately.
			replyOK();

			// Do not return, continue the loop.
		}
	}
}

void ResponderZmq::reply(const std::string& responsePart1, const std::string& responsePart2) {

	// Send the headers.
	for (int i = 0; i < HEADER_SIZE; ++i) {
		zmq::message_t headerPart {m_requestHeader[i].c_str(), m_requestHeader[i].size()};
		m_responder->send(headerPart, zmq::send_flags::sndmore);
	}

	// Send the response in two parts.
	zmq::message_t responsePart1Part {responsePart1.c_str(), responsePart1.size()};
	m_responder->send(responsePart1Part, zmq::send_flags::sndmore);

	zmq::message_t responsePart2Part {responsePart2.c_str(), responsePart2.size()};
	m_responder->send(responsePart2Part, zmq::send_flags::none);
}

void ResponderZmq::replyOK() {

	// Send the headers.
	for (int i = 0; i < HEADER_SIZE; ++i) {
		zmq::message_t headerPart {m_requestHeader[i].c_str(), m_requestHeader[i].size()};
		m_responder->send(headerPart, zmq::send_flags::sndmore);
	}

	std::string result {createRequestResponse(0, "OK")};
	zmq::message_t messagePart {result.c_str(), result.size()};

	m_responder->send(messagePart, zmq::send_flags::none);
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