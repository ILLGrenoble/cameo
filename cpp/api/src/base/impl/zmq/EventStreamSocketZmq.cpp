/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

#include "EventStreamSocketZmq.h"
#include "IdGenerator.h"
#include "Server.h"
#include "ContextZmq.h"
#include "Messages.h"

namespace cameo {

EventStreamSocketZmq::EventStreamSocketZmq() :
	m_context{nullptr} {

}

EventStreamSocketZmq::~EventStreamSocketZmq() {
	terminate();
}

void EventStreamSocketZmq::init(Context * context, const Endpoint& endpoint, RequestSocket * requestSocket) {

	m_context = dynamic_cast<ContextZmq *>(context);

	std::string cancelEndpoint {std::string("inproc://" + IdGenerator::newStringId())};

	// Create the sockets.
	m_cancelSocket = std::unique_ptr<zmq::socket_t>(new zmq::socket_t(m_context->getContext(), zmq::socket_type::pub));
	m_cancelSocket->bind(cancelEndpoint);

	m_socket = std::unique_ptr<zmq::socket_t>(new zmq::socket_t(m_context->getContext(), zmq::socket_type::sub));

	std::vector<std::string> streamList;
	streamList.push_back(message::Event::STATUS);
	streamList.push_back(message::Event::RESULT);
	streamList.push_back(message::Event::KEYVALUE);
	streamList.push_back(message::Event::CANCEL);

	for (std::vector<std::string>::const_iterator s = streamList.begin(); s != streamList.end(); ++s) {
		m_socket->set(zmq::sockopt::subscribe, *s);
	}

	m_socket->connect(endpoint.toString().c_str());
	m_socket->connect(cancelEndpoint);

	// Wait for the connection to be ready.
	// Poll subscriber.
	zmq_pollitem_t items[1];
	items[0].socket = static_cast<void *>(*(m_socket.get()));
	items[0].fd = 0;
	items[0].events = ZMQ_POLLIN;
	items[0].revents = 0;

	while (true) {
		try {
			requestSocket->request(createSyncRequest());
		}
		catch (const ConnectionTimeout& e) {
			// The server is not accessible.
		}

		// Wait for 100ms.
		int rc = zmq::poll(items, 1, std::chrono::milliseconds{100});
		if (rc != 0) {
			break;
		}
	}
}

void EventStreamSocketZmq::send(const std::string& data) {

	zmq::message_t messageData {data.c_str(), data.size()};
	m_socket->send(messageData, zmq::send_flags::none);
}

std::string EventStreamSocketZmq::receive(bool blocking) {

	// Use the message interface.
	zmq::message_t message;

	zmq::recv_flags flags = {(blocking ? zmq::recv_flags::none : zmq::recv_flags::dontwait)};
	if (m_socket->recv(message, flags).has_value()) {
		// The message exists.
		return std::string{message.data<char>(), message.size()};
	}

	return "";
}

void EventStreamSocketZmq::cancel() {

	if (m_cancelSocket.get() != nullptr) {
		std::string data {message::Event::CANCEL};

		zmq::message_t typePart {data.c_str(), data.length()};
		m_cancelSocket->send(typePart, zmq::send_flags::sndmore);

		zmq::message_t dataPart {data.c_str(), data.length()};
		m_cancelSocket->send(dataPart, zmq::send_flags::none);
	}
}

void EventStreamSocketZmq::terminate() {

	if (m_socket) {
		m_socket->close();
		m_socket.reset();
	}
}

}