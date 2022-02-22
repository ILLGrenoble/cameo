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

#include "OutputStreamSocketZmq.h"
#include "Server.h"
#include "ContextZmq.h"
#include "../../Messages.h"
#include "../../CancelIdGenerator.h"

namespace cameo {

OutputStreamSocketZmq::OutputStreamSocketZmq(const std::string& name) :
	m_name(name),
	m_context(nullptr) {
}

OutputStreamSocketZmq::~OutputStreamSocketZmq() {
	close();
}

void OutputStreamSocketZmq::init(Context * context, const Endpoint& endpoint, RequestSocket * requestSocket) {

	m_context = dynamic_cast<ContextZmq *>(context);

	std::stringstream cancelEndpoint;

	// We define a unique name that depends on the event stream socket object because there can be many (instances).
	cancelEndpoint << "inproc://cancel." << CancelIdGenerator::newId();

	// Create the sockets.
	m_cancelSocket = std::unique_ptr<zmq::socket_t>(new zmq::socket_t(m_context->getContext(), zmq::socket_type::pub));
	m_cancelSocket->bind(cancelEndpoint.str());

	m_socket = std::unique_ptr<zmq::socket_t>(new zmq::socket_t(m_context->getContext(), zmq::socket_type::sub));

	std::vector<std::string> topicsList;

	// Get the topic id.
	std::string topicId = StringId::from(message::Event::STREAM, m_name);

	topicsList.push_back(topicId);
	topicsList.push_back(message::Event::CANCEL);

	for (std::vector<std::string>::const_iterator s = topicsList.begin(); s != topicsList.end(); ++s) {
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
			requestSocket->requestJSON(createSyncStreamRequest(m_name));
		}
		catch (const ConnectionTimeout&) {
			// The server is not accessible.
		}

		// Wait for 100ms.
		int rc = zmq::poll(items, 1, 100);

		// Return when the subscriber received a message.
		if (rc != 0) {
			break;
		}
	}
}

void OutputStreamSocketZmq::send(const std::string& data) {

	zmq::message_t messageData(data.c_str(), data.size());
	m_socket->send(messageData, zmq::send_flags::none);
}

std::string OutputStreamSocketZmq::receive(bool blocking) {

	// Use the message interface.
	zmq::message_t message;

	zmq::recv_flags flags = (blocking ? zmq::recv_flags::none : zmq::recv_flags::dontwait);
	if (m_socket->recv(message, flags).has_value()) {
		// The message exists.
		return std::string(message.data<char>(), message.size());
	}

	return "";
}

void OutputStreamSocketZmq::cancel() {

	if (m_cancelSocket.get() != nullptr) {
		std::string data(message::Event::CANCEL);

		zmq::message_t requestType(data.c_str(), data.length());
		m_cancelSocket->send(requestType, zmq::send_flags::sndmore);

		zmq::message_t requestData(data.c_str(), data.length());
		m_cancelSocket->send(requestData, zmq::send_flags::none);
	}
}

void OutputStreamSocketZmq::close() {
	m_socket->close();
}

}
