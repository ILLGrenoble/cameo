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

#include "SubscriberZmq.h"
#include "Serializer.h"
#include "Server.h"
#include "JSON.h"
#include "../../../base/CancelIdGenerator.h"
#include "../../../base/Messages.h"
#include "../../../base/RequestSocket.h"
#include "../../../base/impl/zmq/ContextZmq.h"
#include "../../../coms/impl/PublisherImpl.h"
#include "../../../base/Waiting.h"
#include <sstream>

namespace cameo {
namespace coms {

SubscriberZmq::SubscriberZmq() :
	m_publisherPort(0),
	m_synchronizerPort(0),
	m_numberOfSubscribers(0),
	m_appId(0),
	m_ended(false),
	m_canceled(false) {
}

SubscriberZmq::~SubscriberZmq() {
}

void SubscriberZmq::init(int appId, const Endpoint& appEndpoint, const Endpoint& appStatusEndpoint, int publisherPort, int synchronizerPort, int numberOfSubscribers) {

	m_publisherPort = publisherPort;
	m_synchronizerPort = synchronizerPort;
	m_numberOfSubscribers = numberOfSubscribers;
	m_appId = appId;
	m_ended = false;
	m_canceled = false;

	// Create a socket for publishing.
	ContextZmq* contextImpl = dynamic_cast<ContextZmq *>(application::This::getCom().getContext());
	m_subscriber.reset(new zmq::socket_t(contextImpl->getContext(), ZMQ_SUB));
	m_subscriber->setsockopt(ZMQ_SUBSCRIBE, message::Event::SYNC, std::string(message::Event::SYNC).length());
	m_subscriber->setsockopt(ZMQ_SUBSCRIBE, message::Event::STREAM, std::string(message::Event::STREAM).length());
	m_subscriber->setsockopt(ZMQ_SUBSCRIBE, message::Event::ENDSTREAM, std::string(message::Event::ENDSTREAM).length());
	m_subscriber->setsockopt(ZMQ_SUBSCRIBE, message::Event::CANCEL, std::string(message::Event::CANCEL).length());
	m_subscriber->setsockopt(ZMQ_SUBSCRIBE, message::Event::STATUS, std::string(message::Event::STATUS).length());

	m_subscriber->connect(appEndpoint.withPort(m_publisherPort).toString());

	// We must first bind the cancel publisher before connecting the subscriber.
	std::stringstream cancelEndpoint;

	// We define a unique name.
	cancelEndpoint << "inproc://cancel." << CancelIdGenerator::newId();
	m_cancelEndpoint = cancelEndpoint.str();

	m_cancelPublisher = std::unique_ptr<zmq::socket_t>(new zmq::socket_t(contextImpl->getContext(), ZMQ_PUB));
	m_cancelPublisher->bind(m_cancelEndpoint.c_str());

	m_subscriber->connect(m_cancelEndpoint.c_str());
	m_subscriber->connect(appStatusEndpoint.toString().c_str());

	// Synchronize the subscriber only if the number of subscribers > 0.
	if (m_numberOfSubscribers > 0) {

		// Create a request socket.
		std::unique_ptr<RequestSocket> requestSocket = application::This::getCom().createRequestSocket(appEndpoint.withPort(m_synchronizerPort).toString());

		// Poll subscriber.
		zmq_pollitem_t items[1];
		items[0].socket = static_cast<void *>(*m_subscriber);
		items[0].fd = 0;
		items[0].events = ZMQ_POLLIN;
		items[0].revents = 0;

		while (true) {

			// The subscriber sends SYNC messages to the publisher that returns SYNC message.
			try {
				requestSocket->requestJSON(createSyncRequest());
			}
			catch (const ConnectionTimeout&) {
				// The server is not accessible.
			}

			// Wait for 100ms.
			int rc = zmq::poll(items, 1, 100);
			if (rc != 0) {
				break;
			}
		}

		requestSocket->requestJSON(createSubscribePublisherRequest());
	}
}

bool SubscriberZmq::isEnded() const {
	return m_ended;
}

bool SubscriberZmq::isCanceled() const {
	return m_canceled;
}

std::optional<std::string> SubscriberZmq::receiveBinary() {

	while (true) {
		zmq::message_t message;
		if (!m_subscriber->recv(message, zmq::recv_flags::none).has_value()) {
			return {};
		}

		std::string response(static_cast<char*>(message.data()), message.size());

		if (response == message::Event::STREAM) {
			zmq::message_t secondPart;
			if (!m_subscriber->recv(secondPart, zmq::recv_flags::none).has_value()) {
				return {};
			}
			return std::string(static_cast<char*>(secondPart.data()), secondPart.size());
		}
		else if (response == message::Event::ENDSTREAM) {
			m_ended = true;
			return {};
		}
		else if (response == message::Event::CANCEL) {
			m_canceled = true;
			return {};
		}
		else if (response == message::Event::STATUS) {
			zmq::message_t secondPart;
			if (!m_subscriber->recv(secondPart, zmq::recv_flags::none).has_value()) {
				return {};
			}

			// Get the JSON object.
			json::Object status;
			json::parse(status, secondPart);

			int id = status[message::StatusEvent::ID].GetInt();

			if (id == m_appId) {
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

std::optional<std::string> SubscriberZmq::receive() {
	return receiveBinary();
}

std::optional<std::tuple<std::string, std::string>> SubscriberZmq::receiveTwoBinaryParts() {

	while (true) {
		zmq::message_t message;
		if (!m_subscriber->recv(message, zmq::recv_flags::none).has_value()) {
			return {};
		}

		std::string response(static_cast<char*>(message.data()), message.size());

		if (response == message::Event::STREAM) {

			std::tuple<std::string, std::string> result;

			zmq::message_t secondPart;
			if (!m_subscriber->recv(secondPart, zmq::recv_flags::none).has_value()) {
				return {};
			}
			std::string data1 = std::string(static_cast<char*>(secondPart.data()), secondPart.size());

			zmq::message_t thirdPart;
			if (!m_subscriber->recv(thirdPart, zmq::recv_flags::none).has_value()) {
				return {};
			}
			std::string data2 = std::string(static_cast<char*>(thirdPart.data()), thirdPart.size());

			return std::make_tuple(data1, data2);

		} else if (response == message::Event::ENDSTREAM) {
			m_ended = true;
			return {};

		} else if (response == message::Event::CANCEL) {
			return {};

		} else if (response == message::Event::STATUS) {
			zmq::message_t secondPart;
			if (!m_subscriber->recv(secondPart, zmq::recv_flags::none).has_value()) {
				return {};
			}

			// Get the JSON object.
			json::Object status;
			json::parse(status, secondPart);

			int id = status[message::StatusEvent::ID].GetInt();

			if (id == m_appId) {
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

void SubscriberZmq::cancel() {

	std::string m_message = message::Event::CANCEL;

	zmq::message_t requestType(m_message.length());
	std::string data(message::Event::CANCEL);
	zmq::message_t requestData(data.length());
	memcpy(requestType.data(), m_message.c_str(), m_message.length());
	memcpy(requestData.data(), data.c_str(), data.length());
	m_cancelPublisher->send(requestType, zmq::send_flags::sndmore);
	m_cancelPublisher->send(requestData, zmq::send_flags::none);
}

std::string SubscriberZmq::createSubscribePublisherRequest() const {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushValue(PublisherImpl::SUBSCRIBE_PUBLISHER);

	return request.toString();
}

}
}