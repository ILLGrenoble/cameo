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

RequesterZmq::RequesterZmq() :
	m_pollingTime(100),
	m_timeout(0) {

	m_canceled.store(false);
	m_timedout.store(false);
}

void RequesterZmq::setPollingTime(int value) {
	m_pollingTime = value;
}

void RequesterZmq::setTimeout(int value) {
	m_timeout = value;
}

void RequesterZmq::init(const Endpoint& endpoint, int responderPort) {

	// Create a socket REQ.
	ContextZmq* contextImpl = dynamic_cast<ContextZmq *>(application::This::getCom().getContext());
	m_requester.reset(new zmq::socket_t(contextImpl->getContext(), zmq::socket_type::req));

	// Connect to the endpoint.
	m_requester->connect(endpoint.withPort(responderPort).toString());

	// Configure the socket to not wait at close time.
    int linger = 0;
    m_requester->setsockopt(ZMQ_LINGER, &linger, sizeof(linger));
}

RequesterZmq::~RequesterZmq() {
	terminate();
}

void RequesterZmq::sendRequest(const std::string& requestPart1, const std::string& requestPart2) {

	// Reset timedout.
	m_timedout.store(false);

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

	// Reset timedout.
	m_timedout.store(false);

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

bool RequesterZmq::receiveMessage(zmq::message_t& message) {

	// Define the number of iterations.
	int n = 0;
	if (m_pollingTime > 0) {
		n = m_timeout / m_pollingTime + 1;
	}

	// Infinite loop if timeout is 0 or finite loop if timeout is defined.
	int i = 0;
	while (i < n || m_timeout == 0) {

		// Check if the requester has been canceled.
		if (m_canceled) {
			return false;
		}

		// Poll the requester.
		zmq::pollitem_t items[] = {
			{ *m_requester.get(), 0, ZMQ_POLLIN, 0 }
		};
		zmq::poll(&items[0], 1, m_pollingTime);

		// Get a reply.
		if (items[0].revents & ZMQ_POLLIN) {
			if (!m_requester->recv(message, zmq::recv_flags::none).has_value()) {
				return false;
			}
			return true;
		}
		i++;
	}

	// Timeout.
	m_timedout.store(true);

	return false;
}

std::optional<std::string> RequesterZmq::receiveBinary() {

	if (m_canceled) {
		return {};
	}

	// Receive the message.
	zmq::message_t message;
	if (!receiveMessage(message)) {
		return {};
	}

	// Get the JSON request.
	json::Object request;
	json::parse(request, message);

	int type = request[message::TYPE].GetInt();

	std::optional<std::string> result;

	if (type == message::RESPONSE) {

		// Get the second part of the message.
		zmq::message_t secondPart;
		if (!receiveMessage(secondPart)) {
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

bool RequesterZmq::hasTimedout() {
	return m_timedout;
}

void RequesterZmq::terminate() {

	if (m_requester.get() != nullptr) {
		m_requester.reset(nullptr);
	}
}

}
}
}

