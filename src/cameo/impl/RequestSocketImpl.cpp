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

using namespace std;

namespace cameo {

RequestSocketImpl::RequestSocketImpl(zmq::socket_t * socket, int timeout) :
	m_socket(socket), m_timeout(timeout) {
}

RequestSocketImpl::~RequestSocketImpl() {
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
			m_socket->close();
			throw ConnectionTimeout();
		}
	}

	// Receive the response.
	unique_ptr<zmq::message_t> reply(new zmq::message_t());
	m_socket->recv(reply.get(), 0);

	return reply;
}

}
