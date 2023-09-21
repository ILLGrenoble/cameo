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

#include "RequesterZmq.h"
#include "Application.h"
#include "Messages.h"
#include "JSON.h"
#include "ContextZmq.h"

namespace cameo {
namespace coms {

constexpr int SYNC_TIMEOUT = 200;

RequesterZmq::RequesterZmq() :
	m_pollingTime{100},
	m_timeout{0},
	m_contextImpl{nullptr} {

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
		m_requester.reset();
	}
}

void RequesterZmq::createSocket() {

	// Create a socket dealer.
	// The dealer socket can receive multiple response.
	// It also does not require to provide the identity of the recipient socket that should be done with a socket router.
	m_requester.reset(new zmq::socket_t{m_contextImpl->getContext(), zmq::socket_type::dealer});

	// Connect to the endpoint.
	m_requester->connect(m_endpoint.toString());

	// Configure the socket to not wait at close time.
	int linger {0};
	m_requester->set(zmq::sockopt::linger, linger);
}

void RequesterZmq::createAndSyncSocket(const TimeoutCounter& timeoutCounter) {

	// Memorize the timeout that can have been set before init().
	int previousTimeout = m_timeout;

	// Loop to ensure that the responder is connected to the proxy and can reply.
	m_timeout = SYNC_TIMEOUT;

	while (true) {

		// Init the socket.
		createSocket();

		// Send sync returns false if a timeout occurred.
		if (sendSync()) {
			break;
		}

		// Reset the socket in case of timeout.
		resetSocket();

		// Increase timeout.
		m_timeout += SYNC_TIMEOUT;

		// Check the global timeout.
		if (timeoutCounter.remains() == 0) {
			throw Timeout();
		}
	}

	// Reset timeout.
	m_timeout = previousTimeout;
}

bool RequesterZmq::initSocket() {

	// Reset timedout.
	m_timedout.store(false);

	if (!m_requester) {
		try {
			createAndSyncSocket(TimeoutCounter(m_timeout));
		}
		catch (const Timeout&) {
			// Timeout. As initSocket() is called in sendRequest, we prefer to not throw a timeout exception.
			m_timedout.store(true);

			// Init failed.
			return false;
		}
	}

	return true;
}

bool RequesterZmq::sendSync() {

	json::StringObject jsonRequest;
	jsonRequest.pushKey(message::TYPE);
	jsonRequest.pushValue(message::SYNC);

	// Send the request.
	sendRequest(jsonRequest.dump());

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

void RequesterZmq::init(const Endpoint& endpoint, const std::string& responderIdentity, const TimeoutCounter& timeoutCounter) {

	m_endpoint = endpoint;
	m_responderIdentity = responderIdentity;

	// Get the context.
	m_contextImpl = dynamic_cast<ContextZmq *>(This::getCom().getContext());

	createAndSyncSocket(timeoutCounter);
}

RequesterZmq::~RequesterZmq() {
	resetSocket();
}

void RequesterZmq::sendRequest(const std::string& request) {

	// Init the socket if necessary.
	if (initSocket()) {

		// Start with an empty message for the dealer socket. The identity of the connected router is added by the dealer socket.
		zmq::message_t empty;
		m_requester->send(empty, zmq::send_flags::sndmore);

		zmq::message_t responderIdentityPart {m_responderIdentity.c_str(), m_responderIdentity.size()};
		m_requester->send(responderIdentityPart, zmq::send_flags::sndmore);

		m_requester->send(empty, zmq::send_flags::sndmore);

		zmq::message_t requestPart {request.c_str(), request.size()};
		m_requester->send(requestPart, zmq::send_flags::none);
	}
}

void RequesterZmq::sendRequest(const std::string& requestPart1, const std::string& requestPart2) {

	// Init the socket if necessary.
	if (initSocket()) {

		// Start with an empty message for the dealer socket.
		zmq::message_t empty;
		m_requester->send(empty, zmq::send_flags::sndmore);

		zmq::message_t responderIdentityPart {m_responderIdentity.c_str(), m_responderIdentity.size()};
		m_requester->send(responderIdentityPart, zmq::send_flags::sndmore);

		m_requester->send(empty, zmq::send_flags::sndmore);

		// Send the request in two parts.
		zmq::message_t requestPart1Part {requestPart1.c_str(), requestPart1.size()};
		m_requester->send(requestPart1Part, zmq::send_flags::sndmore);

		zmq::message_t requestPart2Part {requestPart2.c_str(), requestPart2.size()};
		m_requester->send(requestPart2Part, zmq::send_flags::none);
	}
}

void RequesterZmq::sendRequest(const std::string& requestPart1, const std::string& requestPart2, const std::string& requestPart3) {

	// Init the socket if necessary.
	if (initSocket()) {

		// Start with an empty message for the dealer socket.
		zmq::message_t empty;
		m_requester->send(empty, zmq::send_flags::sndmore);

		zmq::message_t responderIdentityPart {m_responderIdentity.c_str(), m_responderIdentity.size()};
		m_requester->send(responderIdentityPart, zmq::send_flags::sndmore);

		m_requester->send(empty, zmq::send_flags::sndmore);

		// Send the request in three parts.
		zmq::message_t requestPart1Part {requestPart1.c_str(), requestPart1.size()};
		m_requester->send(requestPart1Part, zmq::send_flags::sndmore);

		zmq::message_t requestPart2Part {requestPart2.c_str(), requestPart2.size()};
		m_requester->send(requestPart2Part, zmq::send_flags::sndmore);

		zmq::message_t requestPart3Part {requestPart3.c_str(), requestPart3.size()};
		m_requester->send(requestPart3Part, zmq::send_flags::none);
	}
}

void RequesterZmq::send(const std::string& requestData) {

	json::StringObject jsonRequest;
	jsonRequest.pushKey(message::TYPE);
	jsonRequest.pushValue(message::REQUEST);

	jsonRequest.pushKey(message::Request::APPLICATION_NAME);
	jsonRequest.pushValue(This::getName());

	jsonRequest.pushKey(message::Request::APPLICATION_ID);
	jsonRequest.pushValue(This::getId());

	jsonRequest.pushKey(message::Request::SERVER_ENDPOINT);
	jsonRequest.pushValue(This::getEndpoint().toString());

	jsonRequest.pushKey(message::Request::SERVER_PROXY_PORT);
	jsonRequest.pushValue(This::getCom().getResponderProxyPort());

	// Send the request.
	sendRequest(jsonRequest.dump(), requestData);
}

void RequesterZmq::sendTwoParts(const std::string& requestData1, const std::string& requestData2) {

	json::StringObject jsonRequest;
	jsonRequest.pushKey(message::TYPE);
	jsonRequest.pushValue(message::REQUEST);

	jsonRequest.pushKey(message::Request::APPLICATION_NAME);
	jsonRequest.pushValue(This::getName());

	jsonRequest.pushKey(message::Request::APPLICATION_ID);
	jsonRequest.pushValue(This::getId());

	jsonRequest.pushKey(message::Request::SERVER_ENDPOINT);
	jsonRequest.pushValue(This::getEndpoint().toString());

	jsonRequest.pushKey(message::Request::SERVER_PROXY_PORT);
	jsonRequest.pushValue(This::getCom().getResponderProxyPort());

	// Send the request.
	sendRequest(jsonRequest.dump(), requestData1, requestData2);
}

bool RequesterZmq::receiveMessage(zmq::message_t& message) {

	// Define the number of iterations.
	int n {0};
	if (m_pollingTime > 0) {
		n = m_timeout / m_pollingTime + 1;
	}

	// Create the poller.
	zmq::pollitem_t items[] = {
		{ *m_requester.get(), 0, ZMQ_POLLIN, 0 }
	};

	// Infinite loop if timeout is 0 or finite loop if timeout is defined.
	int i {0};
	while (i < n || m_timeout == 0) {

		// Check if the requester has been canceled.
		if (m_canceled) {
			return false;
		}

		// Poll the requester.
		zmq::poll(&items[0], 1, std::chrono::milliseconds{m_pollingTime});

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

std::optional<std::string> RequesterZmq::receive() {

	if (m_canceled) {
		return {};
	}


	// Receive the empty message.
	zmq::message_t empty;
	if (!receiveMessage(empty)) {
		return {};
	}

	// Receive the requester identity.
	zmq::message_t requesterIdentityPart;
	if (!receiveMessage(requesterIdentityPart)) {
		return {};
	}

	// Receive the empty message.
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

	int type {jsonType[message::TYPE].GetInt()};

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

