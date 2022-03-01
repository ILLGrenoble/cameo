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

#include "EventStreamSocketZmq.h"
#include "CancelIdGenerator.h"
#include "Server.h"
#include "ContextZmq.h"
#include "Messages.h"

namespace cameo {

EventStreamSocketZmq::EventStreamSocketZmq() : m_context(nullptr) {

}

EventStreamSocketZmq::~EventStreamSocketZmq() {
	close();
}

void EventStreamSocketZmq::init(Context * context, const Endpoint& endpoint, RequestSocket * requestSocket) {

	m_context = dynamic_cast<ContextZmq *>(context);

	std::stringstream cancelEndpoint;

	// We define a unique name that depends on the event stream socket object because there can be many (instances).
	cancelEndpoint << "inproc://cancel." << CancelIdGenerator::newId();

	// Create the sockets.
	m_cancelSocket = std::unique_ptr<zmq::socket_t>(new zmq::socket_t(m_context->getContext(), zmq::socket_type::pub));
	m_cancelSocket->bind(cancelEndpoint.str());

	m_socket = std::unique_ptr<zmq::socket_t>(new zmq::socket_t(m_context->getContext(), zmq::socket_type::sub));

	std::vector<std::string> streamList;
	streamList.push_back(message::Event::STATUS);
	streamList.push_back(message::Event::RESULT);
	streamList.push_back(message::Event::KEYVALUE);
	streamList.push_back(message::Event::CANCEL);

	for (std::vector<std::string>::const_iterator s = streamList.begin(); s != streamList.end(); ++s) {
		m_socket->setsockopt(ZMQ_SUBSCRIBE, s->c_str(), s->length());
	}

	m_socket->connect(endpoint.toString().c_str());
	m_socket->connect(cancelEndpoint.str().c_str());

	// Wait for the connection to be ready.
	// Poll subscriber.
	zmq_pollitem_t items[1];
	items[0].socket = static_cast<void *>(*(m_socket.get()));
	items[0].fd = 0;
	items[0].events = ZMQ_POLLIN;
	items[0].revents = 0;

	while (true) {
		try {
			requestSocket->requestJSON(createSyncRequest());
		}
		catch (const ConnectionTimeout& e) {
			// The server is not accessible.
		}

		// Wait for 100ms.
		int rc = zmq::poll(items, 1, 100);
		if (rc != 0) {
			break;
		}
	}
}

void EventStreamSocketZmq::send(const std::string& data) {

	zmq::message_t messageData(data.c_str(), data.size());
	m_socket->send(messageData, zmq::send_flags::none);
}

std::string EventStreamSocketZmq::receive(bool blocking) {

	// Use the message interface.
	zmq::message_t message;

	zmq::recv_flags flags = (blocking ? zmq::recv_flags::none : zmq::recv_flags::dontwait);
	if (m_socket->recv(message, flags).has_value()) {
		// The message exists.
		return std::string(message.data<char>(), message.size());
	}

	return "";
}

void EventStreamSocketZmq::cancel() {

	if (m_cancelSocket.get() != nullptr) {
		std::string data(message::Event::CANCEL);

		zmq::message_t typePart(data.c_str(), data.length());
		m_cancelSocket->send(typePart, zmq::send_flags::sndmore);

		zmq::message_t dataPart(data.c_str(), data.length());
		m_cancelSocket->send(dataPart, zmq::send_flags::none);
	}
}

void EventStreamSocketZmq::close() {
	m_socket->close();
}

}
