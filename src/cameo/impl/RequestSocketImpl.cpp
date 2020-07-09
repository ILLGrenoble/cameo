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

#include "RequestSocketImpl.h"
#include "../ConnectionTimeout.h"
#include <iostream>

#include <chrono>
#include <thread>

using namespace std;

namespace cameo {

RequestSocketImpl::RequestSocketImpl(zmq::socket_t * socket, int timeout) :
	m_socket(socket) {

	setTimeout(timeout);
}

RequestSocketImpl::~RequestSocketImpl() {
}

void RequestSocketImpl::setTimeout(int timeout) {
	m_timeout = timeout;

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

std::unique_ptr<zmq::message_t> RequestSocketImpl::request(const std::string& requestTypePart, const std::string& requestDataPart, int overrideTimeout) {

	// Prepare the request parts.
	int requestTypeSize = requestTypePart.length();
	int requestDataSize = requestDataPart.length();
	zmq::message_t requestType(requestTypeSize);
	zmq::message_t requestData(requestDataSize);
	memcpy(static_cast<void *>(requestType.data()), requestTypePart.c_str(), requestTypeSize);
	memcpy(static_cast<void *>(requestData.data()), requestDataPart.c_str(), requestDataSize);

	// Send the request in two parts.
	m_socket->send(requestType, ZMQ_SNDMORE);
	m_socket->send(requestData);

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
			// Timeout occurred.
			throw ConnectionTimeout();
		}
	}

	// Receive the response.
	unique_ptr<zmq::message_t> reply(new zmq::message_t());
	m_socket->recv(reply.get(), 0);

	return reply;
}

}
