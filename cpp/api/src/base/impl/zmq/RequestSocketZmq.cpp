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

#include "SocketException.h"
#include "ConnectionTimeout.h"
#include "ContextZmq.h"
#include <iostream>
#include <chrono>
#include <thread>

using namespace std;

namespace cameo {

RequestSocketZmq::RequestSocketZmq(Context * context, const std::string& endpoint, const std::string& responderIdentity) :
	m_context{dynamic_cast<ContextZmq *>(context)},
	m_endpoint{endpoint},
	m_responderIdentity{responderIdentity},
	m_timeout{0} {

	init();
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
	if (m_timeout > 0 && m_socket) {
		int lingerValue {100};
		m_socket->setsockopt(ZMQ_LINGER, &lingerValue, sizeof(int));
	}
}

void RequestSocketZmq::init() {

	// Reset if the socket is null.
	if (!m_socket) {
		m_socket = std::unique_ptr<zmq::socket_t>{new zmq::socket_t{m_context->getContext(), zmq::socket_type::req}};

		// Set the linger value to 0 to ensure that pending requests are destroyed in case of timeout.
		int value {0};
		m_socket->setsockopt(ZMQ_LINGER, &value, sizeof(int));

		try {
			// Connect to the endpoint.
			m_socket->connect(m_endpoint.c_str());
		}
		catch (exception const & e) {
			throw SocketException(e.what());
		}

		// Apply the linger to the socket.
		setSocketLinger();
	}
}

void RequestSocketZmq::reset() {
	m_socket.reset();
}

std::unique_ptr<zmq::message_t> RequestSocketZmq::receive(int overrideTimeout) {

	int timeout {m_timeout};
	if (overrideTimeout > -1) {
		timeout = overrideTimeout;
	}

	if (timeout == -2) {
		return {};
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
	// Do not keep the first two parts as they contain the responder identity and an empty part.
	zmq::message_t identity, empty;
	if (!m_socket->recv(identity, zmq::recv_flags::none).has_value()) {
		return {};
	}

	if (!m_socket->recv(empty, zmq::recv_flags::none).has_value()) {
		return {};
	}

	unique_ptr<zmq::message_t> reply {new zmq::message_t{}};
	if (m_socket->recv(*reply.get(), zmq::recv_flags::none).has_value()) {
		return reply;
	}

	return {};
}

std::string RequestSocketZmq::request(const std::string& request, int overrideTimeout) {

	// Init if not already done or if a timeout occurred.
	init();

	// Prepare the request parts.
	zmq::message_t identityPart {m_responderIdentity.c_str(), m_responderIdentity.size()};
	m_socket->send(identityPart, zmq::send_flags::sndmore);

	zmq::message_t empty;
	m_socket->send(empty, zmq::send_flags::sndmore);

	zmq::message_t requestPart {request.c_str(), request.length()};
	m_socket->send(requestPart, zmq::send_flags::none);

	// Receive and return the response.
	std::unique_ptr<zmq::message_t> response {receive(overrideTimeout)};

	return std::string{response->data<char>(), response->size()};
}

std::string RequestSocketZmq::request(const std::string& requestPart1, const std::string& requestPart2, int overrideTimeout) {

	// Init if not already done or if a timeout occurred.
	init();

	// Prepare the request parts.
	zmq::message_t identityPart {m_responderIdentity.c_str(), m_responderIdentity.size()};
	m_socket->send(identityPart, zmq::send_flags::sndmore);

	zmq::message_t empty;
	m_socket->send(empty, zmq::send_flags::sndmore);

	zmq::message_t requestPart1Part {requestPart1.c_str(), requestPart1.length()};
	m_socket->send(requestPart1Part, zmq::send_flags::sndmore);

	zmq::message_t requestPart2Part {requestPart2.c_str(), requestPart2.length()};
	m_socket->send(requestPart2Part, zmq::send_flags::none);

	// Receive and return the response.
	std::unique_ptr<zmq::message_t> response {receive(overrideTimeout)};

	return std::string{response->data<char>(), response->size()};
}

std::string RequestSocketZmq::request(const std::string& requestPart1, const std::string& requestPart2, const std::string& requestPart3, int overrideTimeout) {

	// Init if not already done or if a timeout occurred.
	init();

	// Prepare the request parts.
	zmq::message_t identityPart {m_responderIdentity.c_str(), m_responderIdentity.size()};
	m_socket->send(identityPart, zmq::send_flags::sndmore);

	zmq::message_t empty;
	m_socket->send(empty, zmq::send_flags::sndmore);

	zmq::message_t requestPart1Part {requestPart1.c_str(), requestPart1.length()};
	m_socket->send(requestPart1Part, zmq::send_flags::sndmore);

	zmq::message_t requestPart2Part {requestPart2.c_str(), requestPart2.length()};
	m_socket->send(requestPart2Part, zmq::send_flags::sndmore);

	zmq::message_t requestPart3Part {requestPart3.c_str(), requestPart3.length()};
	m_socket->send(requestPart3Part, zmq::send_flags::none);

	// Receive and return the response.
	std::unique_ptr<zmq::message_t> response {receive(overrideTimeout)};

	return std::string{response->data<char>(), response->size()};
}

}
