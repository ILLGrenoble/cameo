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

#include "BasicRequesterZmq.h"
#include "Application.h"
#include "Serializer.h"
#include "JSON.h"
#include "../../../base/impl/zmq/ContextZmq.h"
#include "../../../base/Messages.h"
#include <zmq.hpp>
#include <sstream>

namespace cameo {
namespace coms {
namespace basic {

void RequesterZmq::init(const Endpoint& endpoint, int responderPort) {

	m_canceled.store(false);

	// Create a socket REQ.
	ContextZmq* contextImpl = dynamic_cast<ContextZmq *>(application::This::getCom().getContext());
	m_requester.reset(new zmq::socket_t(contextImpl->getContext(), ZMQ_REQ));

	m_requester->connect(endpoint.withPort(responderPort).toString());
}

RequesterZmq::~RequesterZmq() {
	terminate();
}

void RequesterZmq::sendRequest(const std::string& requestPart1, const std::string& requestPart2) {

	// Prepare the request parts.
	int requestPart1Size = requestPart1.length();
	int requestPart2Size = requestPart2.length();
	zmq::message_t requestPart1Message(requestPart1Size);
	zmq::message_t requestPart2Message(requestPart2Size);
	memcpy(static_cast<void *>(requestPart1Message.data()), requestPart1.c_str(), requestPart1Size);
	memcpy(static_cast<void *>(requestPart2Message.data()), requestPart2.c_str(), requestPart2Size);

	// Send the request in two parts.
	m_requester->send(requestPart1Message, zmq::send_flags::sndmore);
	m_requester->send(requestPart2Message, zmq::send_flags::none);
}

void RequesterZmq::sendRequest(const std::string& requestPart1, const std::string& requestPart2, const std::string& requestPart3) {

	// Prepare the request parts.
	int requestPart1Size = requestPart1.length();
	int requestPart2Size = requestPart2.length();
	int requestPart3Size = requestPart3.length();
	zmq::message_t requestPart1Message(requestPart1Size);
	zmq::message_t requestPart2Message(requestPart2Size);
	zmq::message_t requestPart3Message(requestPart3Size);
	memcpy(static_cast<void *>(requestPart1Message.data()), requestPart1.c_str(), requestPart1Size);
	memcpy(static_cast<void *>(requestPart2Message.data()), requestPart2.c_str(), requestPart2Size);
	memcpy(static_cast<void *>(requestPart3Message.data()), requestPart3.c_str(), requestPart3Size);

	// Send the request in three parts.
	m_requester->send(requestPart1Message, zmq::send_flags::sndmore);
	m_requester->send(requestPart2Message, zmq::send_flags::sndmore);
	m_requester->send(requestPart3Message, zmq::send_flags::none);
}

void RequesterZmq::sendBinary(const std::string& requestData) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushValue(message::REQUEST);

	request.pushKey(message::Request::APPLICATION_NAME);
	request.pushValue(application::This::getName());

	request.pushKey(message::Request::APPLICATION_ID);
	request.pushValue(application::This::getId());

	request.pushKey(message::Request::SERVER_URL);
	request.pushValue(application::This::getEndpoint().getProtocol() + "://" + application::This::getEndpoint().getAddress());

	request.pushKey(message::Request::SERVER_PORT);
	request.pushValue(application::This::getEndpoint().getPort());

	// Send the request.
	sendRequest(request.toString(), requestData);
}

void RequesterZmq::send(const std::string& requestData) {

	std::string result;
	serialize(requestData, result);
	sendBinary(result);
}

void RequesterZmq::sendTwoBinaryParts(const std::string& requestData1, const std::string& requestData2) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushValue(message::REQUEST);

	request.pushKey(message::Request::APPLICATION_NAME);
	request.pushValue(application::This::getName());

	request.pushKey(message::Request::APPLICATION_ID);
	request.pushValue(application::This::getId());

	request.pushKey(message::Request::SERVER_URL);
	request.pushValue(application::This::getEndpoint().getProtocol() + "://" + application::This::getEndpoint().getAddress());

	request.pushKey(message::Request::SERVER_PORT);
	request.pushValue(application::This::getEndpoint().getPort());

	// Send the request.
	sendRequest(request.toString(), requestData1, requestData2);
}

std::optional<std::string> RequesterZmq::receiveBinary() {

	if (m_canceled) {
		return {};
	}

	zmq::message_t message;
	if (!m_requester->recv(message, zmq::recv_flags::none).has_value()) {
		return {};
	}

	// Get the JSON request.
	json::Object request;
	json::parse(request, message);

	int type = request[message::TYPE].GetInt();

	std::optional<std::string> result;

	if (type == message::RESPONSE) {

		// Get the second part for the message.
		zmq::message_t secondPart;
		if (!m_requester->recv(secondPart, zmq::recv_flags::none).has_value()) {
			return {};
		}
		result = std::string(secondPart.data<char>(), secondPart.size());
	}

	return result;
}

std::optional<std::string> RequesterZmq::receive() {
	return receiveBinary();
}

void RequesterZmq::cancel() {
	m_canceled.store(true);
}

bool RequesterZmq::isCanceled() {
	return m_canceled;
}

void RequesterZmq::terminate() {

	if (m_requester.get() != nullptr) {
		m_requester.reset(nullptr);
	}
}

}
}
}

