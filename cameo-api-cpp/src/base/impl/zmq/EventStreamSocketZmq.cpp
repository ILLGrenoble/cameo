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
#include "Server.h"
#include "ContextZmq.h"
#include "../../Messages.h"
#include "../../CancelIdGenerator.h"

using namespace std;

namespace cameo {

EventStreamSocketZmq::EventStreamSocketZmq(Server * server) :
	m_server(server) {
	m_context = dynamic_cast<ContextZmq *>(server->getContext());
}

EventStreamSocketZmq::~EventStreamSocketZmq() {
	close();
}

void EventStreamSocketZmq::init() {

	std::stringstream cancelEndpoint;

	// We define a unique name that depends on the event stream socket object because there can be many (instances).
	cancelEndpoint << "inproc://cancel." << CancelIdGenerator::newId();

	// Create the sockets.
	m_cancelSocket = std::unique_ptr<zmq::socket_t>(new zmq::socket_t(m_context->getContext(), ZMQ_PUB));
	m_cancelSocket->bind(cancelEndpoint.str());


	m_socket.reset(m_context->createEventSubscriber(m_server->getStatusEndpoint().toString(), cancelEndpoint.str()));

	// Wait for the connection to be ready.
	// Poll subscriber.
	zmq_pollitem_t items[1];
	items[0].socket = static_cast<void *>(*(m_socket.get()));
	items[0].fd = 0;
	items[0].events = ZMQ_POLLIN;
	items[0].revents = 0;

	while (true) {
		m_server->sendSync();

		// Wait for 100ms.
		int rc = zmq::poll(items, 1, 100);
		if (rc != 0) {
			break;
		}
	}
}

void EventStreamSocketZmq::send(const std::string& data) {

	zmq::message_t messageData(data.size());
	memcpy((void *) messageData.data(), data.c_str(), data.size());
	m_socket->send(messageData);
}

std::string EventStreamSocketZmq::receive(bool blocking) {

	// Use the message interface.
	unique_ptr<zmq::message_t> message(new zmq::message_t());
	if (m_socket->recv(message.get(), (blocking ? 0 : ZMQ_DONTWAIT))) {
		// The message exists.
		return std::string(message->data<char>(), message->size());
	}

	return "";
}

void EventStreamSocketZmq::cancel() {

	if (m_cancelSocket.get() != nullptr) {
		string data(message::Event::CANCEL);
		zmq::message_t requestType(data.length());
		zmq::message_t requestData(data.length());
		memcpy(requestType.data(), message::Event::CANCEL, data.length());
		memcpy(requestData.data(), data.c_str(), data.length());
		m_cancelSocket->send(requestType, ZMQ_SNDMORE);
		m_cancelSocket->send(requestData);
	}
}

void EventStreamSocketZmq::close() {
	m_socket->close();
}

}
