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
#include "Messages.h"
#include "JSON.h"
#include "../../../base/impl/zmq/ContextZmq.h"
#include <zmq.hpp>
#include <sstream>

namespace cameo {
namespace coms {
namespace basic {

constexpr int SYNC_TIMEOUT = 200;

RequesterZmq::RequesterZmq() :
	m_pollingTime(100),
	m_timeout(0),
	m_contextImpl(nullptr) {

	m_canceled.store(false);
	m_timedout.store(false);
}

void RequesterZmq::setPollingTime(int value) {
	m_pollingTime = value;
}

void RequesterZmq::setTimeout(int value) {
	m_timeout = value;
}

void RequesterZmq::resetSocket() {
	if (m_requester) {
		m_requester.reset(nullptr);
	}
}

void RequesterZmq::initSocket() {

	if (!m_requester) {
		// Create a socket REQ.
		m_requester.reset(new zmq::socket_t(m_contextImpl->getContext(), zmq::socket_type::req));

		// Connect to the endpoint.
		m_requester->connect(m_endpoint.toString());

		// Configure the socket to not wait at close time.
		int linger = 0;
		m_requester->setsockopt(ZMQ_LINGER, &linger, sizeof(linger));
	}
}

bool RequesterZmq::sendSync() {

	json::StringObject jsonRequest;
	jsonRequest.pushKey(message::TYPE);
	jsonRequest.pushValue(message::SYNC);

	// Send the request.
	sendRequest(jsonRequest.toString());

	while (true) {

		zmq::message_t message;
		if (!receiveMessage(message)) {
			return false;
		}

		if (!message.more()) {
			break;
		}
	}

	// Had a response we can exit the loop.
	return true;
}

void RequesterZmq::init(const Endpoint& endpoint, const std::string& responderIdentity) {

	m_endpoint = endpoint;
	m_responderIdentity = responderIdentity;

	// Get the context.
	m_contextImpl = dynamic_cast<ContextZmq *>(application::This::getCom().getContext());

	m_timeout = SYNC_TIMEOUT;

	while (true) {

		// Init the socket.
		initSocket();

		// Send sync returns false if a timeout occurred.
		if (sendSync()) {
			break;
		}

		// Reset the socket in case of timeout.
		resetSocket();

		// Increase timeout.
		m_timeout += SYNC_TIMEOUT;
	}

	// Reset timeout.
	m_timeout = 0;
}

RequesterZmq::~RequesterZmq() {
	resetSocket();
}

void RequesterZmq::sendRequest(const std::string& request) {

	// Reset timedout.
	m_timedout.store(false);

	// Init the socket if necessary.
	initSocket();

	// Add the responder identity as first part.
	zmq::message_t responderIdentityPart(m_responderIdentity.c_str(), m_responderIdentity.size());
	m_requester->send(responderIdentityPart, zmq::send_flags::sndmore);

	zmq::message_t empty;
	m_requester->send(empty, zmq::send_flags::sndmore);

	zmq::message_t requestPart(request.c_str(), request.size());
	m_requester->send(requestPart, zmq::send_flags::none);
}

void RequesterZmq::sendRequest(const std::string& requestPart1, const std::string& requestPart2) {

	// Reset timedout.
	m_timedout.store(false);

	// Init the socket if necessary.
	initSocket();

	// Add the responder identity as first part.
	zmq::message_t responderIdentityPart(m_responderIdentity.c_str(), m_responderIdentity.size());
	m_requester->send(responderIdentityPart, zmq::send_flags::sndmore);

	zmq::message_t empty;
	m_requester->send(empty, zmq::send_flags::sndmore);

	// Send the request in two parts.
	zmq::message_t requestPart1Part(requestPart1.c_str(), requestPart1.size());
	m_requester->send(requestPart1Part, zmq::send_flags::sndmore);

	zmq::message_t requestPart2Part(requestPart2.c_str(), requestPart2.size());
	m_requester->send(requestPart2Part, zmq::send_flags::none);
}

void RequesterZmq::sendRequest(const std::string& requestPart1, const std::string& requestPart2, const std::string& requestPart3) {

	// Reset timedout.
	m_timedout.store(false);

	// Init the socket if necessary.
	initSocket();

	// Add the responder identity as first part.
	zmq::message_t responderIdentityPart(m_responderIdentity.c_str(), m_responderIdentity.size());
	m_requester->send(responderIdentityPart, zmq::send_flags::sndmore);

	zmq::message_t empty;
	m_requester->send(empty, zmq::send_flags::sndmore);

	// Send the request in three parts.
	zmq::message_t requestPart1Part(requestPart1.c_str(), requestPart1.size());
	m_requester->send(requestPart1Part, zmq::send_flags::sndmore);

	zmq::message_t requestPart2Part(requestPart2.c_str(), requestPart2.size());
	m_requester->send(requestPart2Part, zmq::send_flags::sndmore);

	zmq::message_t requestPart3Part(requestPart3.c_str(), requestPart3.size());
	m_requester->send(requestPart3Part, zmq::send_flags::none);
}

void RequesterZmq::sendBinary(const std::string& requestData) {

	json::StringObject jsonRequest;
	jsonRequest.pushKey(message::TYPE);
	jsonRequest.pushValue(message::REQUEST);

	jsonRequest.pushKey(message::Request::APPLICATION_NAME);
	jsonRequest.pushValue(application::This::getName());

	jsonRequest.pushKey(message::Request::APPLICATION_ID);
	jsonRequest.pushValue(application::This::getId());

	jsonRequest.pushKey(message::Request::SERVER_ENDPOINT);
	jsonRequest.pushValue(application::This::getEndpoint().toString());

	jsonRequest.pushKey(message::Request::SERVER_PROXY_PORT);
	jsonRequest.pushValue(application::This::getCom().getResponderProxyPort());

	// Send the request.
	sendRequest(jsonRequest.toString(), requestData);
}

void RequesterZmq::send(const std::string& requestData) {
	sendBinary(requestData);
}

void RequesterZmq::sendTwoBinaryParts(const std::string& requestData1, const std::string& requestData2) {

	json::StringObject jsonRequest;
	jsonRequest.pushKey(message::TYPE);
	jsonRequest.pushValue(message::REQUEST);

	jsonRequest.pushKey(message::Request::APPLICATION_NAME);
	jsonRequest.pushValue(application::This::getName());

	jsonRequest.pushKey(message::Request::APPLICATION_ID);
	jsonRequest.pushValue(application::This::getId());

	jsonRequest.pushKey(message::Request::SERVER_ENDPOINT);
	jsonRequest.pushValue(application::This::getEndpoint().toString());

	jsonRequest.pushKey(message::Request::SERVER_PROXY_PORT);
	jsonRequest.pushValue(application::This::getCom().getResponderProxyPort());

	// Send the request.
	sendRequest(jsonRequest.toString(), requestData1, requestData2);
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

	// Reset the socket because it cannot be reused after a timeout.
	resetSocket();

	return false;
}

std::optional<std::string> RequesterZmq::receiveBinary() {

	if (m_canceled) {
		return {};
	}

	// Receive the requester identity.
	zmq::message_t requesterIdentityPart;
	if (!receiveMessage(requesterIdentityPart)) {
		return {};
	}

	// Receive the empty message.
	zmq::message_t empty;
	if (!receiveMessage(empty)) {
		return {};
	}

	// Receive the message.
	zmq::message_t typePart;
	if (!receiveMessage(typePart)) {
		return {};
	}

	// Get the JSON type.
	json::Object jsonType;
	json::parse(jsonType, typePart);

	int type = jsonType[message::TYPE].GetInt();

	std::optional<std::string> result;

	if (type == message::RESPONSE) {

		// Get the response part of the message.
		zmq::message_t responsePart;
		if (!receiveMessage(responsePart)) {
			return {};
		}

		result = std::string(responsePart.data<char>(), responsePart.size());
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
	resetSocket();
}

}
}
}

