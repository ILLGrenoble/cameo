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

#include "RequestSocketZmq.h"

#include "ConnectionTimeout.h"
#include "ContextZmq.h"
#include <iostream>
#include <chrono>
#include <thread>

using namespace std;

namespace cameo {

RequestSocketZmq::RequestSocketZmq(Context * context, const std::string& endpoint, int timeout) :
	m_services(dynamic_cast<ContextZmq *>(context)), m_endpoint(endpoint) {

	init();

	setTimeout(timeout);
}

RequestSocketZmq::~RequestSocketZmq() {
}

void RequestSocketZmq::setTimeout(int timeout) {
	m_timeout = timeout;

	// Apply the linger to the socket.
	setSocketLinger();
}

void RequestSocketZmq::setSocketLinger() {
	// Set the linger in case of timeout.
	// If not, the context can block indefinitely.
	// Does the value 100 can lead to a side-effect? A too small value like 1 has some side-effect.
	// After some tests, the value seems reasonable.
	// If a Server instance is not reachable, the context that contains the message in timeout will block during this linger period.
	if (m_timeout > 0) {
		int lingerValue = 100;
		m_socket->setsockopt(ZMQ_LINGER, &lingerValue, sizeof(int));
	}
}

void RequestSocketZmq::init() {

	// Reset if the socket is null.
	if (m_socket.get() == nullptr) {
		m_socket.reset(m_services->createRequestSocket(m_endpoint));

		// Apply the linger to the socket.
		setSocketLinger();
	}
}

void RequestSocketZmq::reset() {
	m_socket.reset();
}

std::unique_ptr<zmq::message_t> RequestSocketZmq::receive(int overrideTimeout) {

	int timeout = m_timeout;
	if (overrideTimeout > -1) {
		timeout = overrideTimeout;
	}

	if (timeout == -2) {
		return unique_ptr<zmq::message_t>(nullptr);
	}

	if (timeout > 0) {
		// Polling.
		zmq_pollitem_t items[1];
		items[0].socket = static_cast<void *>(*m_socket.get());
		items[0].fd = 0;
		items[0].events = ZMQ_POLLIN;
		items[0].revents = 0;

		int rc = zmq::poll(items, 1, timeout);
		if (rc == 0) {
			// Reset the socket. It is necessary if a new request is done.
			reset();

			// Timeout occurred.
			throw ConnectionTimeout();
		}
	}

	// Receive the response.
	unique_ptr<zmq::message_t> reply(new zmq::message_t());
	m_socket->recv(reply.get(), 0);

	return reply;
}

std::string RequestSocketZmq::request(const std::string& request, int overrideTimeout) {

	// Init if not already done or if a timeout occurred.
	init();

	// Prepare the request parts.
	int requestSize = request.length();
	zmq::message_t requestMessage(requestSize);
	memcpy(static_cast<void *>(requestMessage.data()), request.c_str(), requestSize);

	// Send the request in one part.
	m_socket->send(requestMessage);

	// Receive and return the response.
	std::unique_ptr<zmq::message_t> response = receive(overrideTimeout);

	return std::string(response->data<char>(), response->size());
}

std::string RequestSocketZmq::request(const std::string& requestPart1, const std::string& requestPart2, int overrideTimeout) {

	// Init if not already done or if a timeout occurred.
	init();

	// Prepare the request parts.
	int requestPart1Size = requestPart1.length();
	int requestPart2Size = requestPart2.length();
	zmq::message_t requestPart1Message(requestPart1Size);
	zmq::message_t requestPart2Message(requestPart2Size);
	memcpy(static_cast<void *>(requestPart1Message.data()), requestPart1.c_str(), requestPart1Size);
	memcpy(static_cast<void *>(requestPart2Message.data()), requestPart2.c_str(), requestPart2Size);

	// Send the request in two parts.
	m_socket->send(requestPart1Message, ZMQ_SNDMORE);
	m_socket->send(requestPart2Message);

	// Receive and return the response.
	std::unique_ptr<zmq::message_t> response = receive(overrideTimeout);

	return std::string(response->data<char>(), response->size());
}

std::string RequestSocketZmq::request(const std::string& requestPart1, const std::string& requestPart2, const std::string& requestPart3, int overrideTimeout) {

	// Init if not already done or if a timeout occurred.
	init();

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
	m_socket->send(requestPart1Message, ZMQ_SNDMORE);
	m_socket->send(requestPart2Message, ZMQ_SNDMORE);
	m_socket->send(requestPart3Message);

	// Receive and return the response.
	std::unique_ptr<zmq::message_t> response = receive(overrideTimeout);

	return std::string(response->data<char>(), response->size());
}

}