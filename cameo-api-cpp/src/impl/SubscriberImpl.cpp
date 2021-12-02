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

#include "SubscriberImpl.h"
#include "Serializer.h"
#include "CancelIdGenerator.h"
#include "ServicesImpl.h"
#include "RequestSocketImpl.h"
#include "Server.h"
#include "message/Message.h"
#include "JSON.h"
#include <sstream>

using namespace std;

namespace cameo {

SubscriberImpl::SubscriberImpl(Server * server, int publisherPort, int synchronizerPort, const std::string& publisherName, int numberOfSubscribers, const std::string& instanceName, int instanceId, const std::string& instanceEndpoint, const std::string& statusEndpoint) :
	m_server(server), // server associated with instance
	m_publisherName(publisherName),
	m_publisherPort(publisherPort),
	m_synchronizerPort(synchronizerPort),
	m_numberOfSubscribers(numberOfSubscribers),
	m_instanceName(instanceName),
	m_instanceId(instanceId),
	m_instanceEndpoint(instanceEndpoint), // endpoint of server
	m_statusEndpoint(statusEndpoint), // status endpoint of server
	m_ended(false),
	m_canceled(false) {
}

SubscriberImpl::~SubscriberImpl() {
}

void SubscriberImpl::init() {

	// Create a socket for publishing.
	m_subscriber.reset(new zmq::socket_t(m_server->m_impl->m_context, ZMQ_SUB));
	m_subscriber->setsockopt(ZMQ_SUBSCRIBE, message::Event::SYNC, string(message::Event::SYNC).length());
	m_subscriber->setsockopt(ZMQ_SUBSCRIBE, message::Event::STREAM, string(message::Event::STREAM).length());
	m_subscriber->setsockopt(ZMQ_SUBSCRIBE, message::Event::ENDSTREAM, string(message::Event::ENDSTREAM).length());
	m_subscriber->setsockopt(ZMQ_SUBSCRIBE, message::Event::CANCEL, string(message::Event::CANCEL).length());
	m_subscriber->setsockopt(ZMQ_SUBSCRIBE, message::Event::STATUS, string(message::Event::STATUS).length());

	m_subscriber->connect(m_server->getEndpoint().withPort(m_publisherPort).toString());

	// We must first bind the cancel publisher before connecting the subscriber.
	stringstream cancelEndpoint;

	// We define a unique name.
	cancelEndpoint << "inproc://cancel." << CancelIdGenerator::newId();
	m_cancelEndpoint = cancelEndpoint.str();

	m_cancelPublisher = unique_ptr<zmq::socket_t>(new zmq::socket_t(m_server->m_impl->m_context, ZMQ_PUB));
	m_cancelPublisher->bind(m_cancelEndpoint.c_str());

	m_subscriber->connect(m_cancelEndpoint.c_str());
	m_subscriber->connect(m_statusEndpoint.c_str());

	// Synchronize the subscriber only if the number of subscribers > 0.
	if (m_numberOfSubscribers > 0) {

		// Create a request socket.
		unique_ptr<RequestSocketImpl> requestSocket = m_server->createRequestSocket(m_server->getEndpoint().withPort(m_synchronizerPort).toString());

		// Poll subscriber.
		zmq_pollitem_t items[1];
		items[0].socket = static_cast<void *>(*m_subscriber);
		items[0].fd = 0;
		items[0].events = ZMQ_POLLIN;
		items[0].revents = 0;

		while (true) {
			m_server->m_impl->isAvailable(requestSocket.get(), 100);

			// Wait for 100ms.
			int rc = zmq::poll(items, 1, 100);
			if (rc != 0) {
				break;
			}
		}

		requestSocket->request(createSubscribePublisherRequest());
	}
}

bool SubscriberImpl::isEnded() const {
	return m_ended;
}

bool SubscriberImpl::isCanceled() const {
	return m_canceled;
}

std::optional<std::string> SubscriberImpl::receiveBinary() {

	while (true) {
		unique_ptr<zmq::message_t> message(new zmq::message_t());
		m_subscriber->recv(message.get());

		string response(static_cast<char*>(message->data()), message->size());

		if (response == message::Event::STREAM) {
			message.reset(new zmq::message_t());
			m_subscriber->recv(message.get());
			return string(static_cast<char*>(message->data()), message->size());

		} else if (response == message::Event::ENDSTREAM) {
			m_ended = true;
			return {};

		} else if (response == message::Event::CANCEL) {
			m_canceled = true;
			return {};

		} else if (response == message::Event::STATUS) {
			message.reset(new zmq::message_t());
			m_subscriber->recv(message.get());

			// Get the JSON object.
			json::Object status;
			json::parse(status, message.get());

			int id = status[message::StatusEvent::ID].GetInt();

			if (id == m_instanceId) {
				application::State state = status[message::StatusEvent::APPLICATION_STATE].GetInt();

				// test the terminal state
				if (state == application::SUCCESS
					|| state == application::STOPPED
					|| state == application::KILLED
					|| state == application::FAILURE) {
					// Exit because the remote application has terminated.
					return {};
				}
			}
		}
	}

	return {};
}

std::optional<std::string> SubscriberImpl::receive() {
	return receiveBinary();
}

std::optional<std::tuple<std::string, std::string>> SubscriberImpl::receiveTwoBinaryParts() {

	while (true) {
		unique_ptr<zmq::message_t> message(new zmq::message_t());
		m_subscriber->recv(message.get());

		string response(static_cast<char*>(message->data()), message->size());

		if (response == message::Event::STREAM) {

			std::tuple<std::string, std::string> result;

			message.reset(new zmq::message_t());
			m_subscriber->recv(message.get());
			string data1 = string(static_cast<char*>(message->data()), message->size());

			message.reset(new zmq::message_t());
			m_subscriber->recv(message.get());
			string data2 = string(static_cast<char*>(message->data()), message->size());

			return make_tuple(data1, data2);

		} else if (response == message::Event::ENDSTREAM) {
			m_ended = true;
			return {};

		} else if (response == message::Event::CANCEL) {
			return {};

		} else if (response == message::Event::STATUS) {
			message.reset(new zmq::message_t());
			m_subscriber->recv(message.get());

			// Get the JSON object.
			json::Object status;
			json::parse(status, message.get());

			int id = status[message::StatusEvent::ID].GetInt();

			if (id == m_instanceId) {
				application::State state = status[message::StatusEvent::APPLICATION_STATE].GetInt();

				// test the terminal state
				if (state == application::SUCCESS
					|| state == application::STOPPED
					|| state == application::KILLED
					|| state == application::FAILURE) {
					// Exit because the remote application has terminated.
					return {};
				}
			}
		}
	}

	return {};
}

WaitingImpl * SubscriberImpl::waiting() {

	// Waiting gets the cancel publisher.
	return new SocketWaitingImpl(m_cancelPublisher.get(), message::Event::CANCEL);
}

std::string SubscriberImpl::createSubscribePublisherRequest() const {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushInt(message::SUBSCRIBE_PUBLISHER_v0);

	return request.toString();
}


}
