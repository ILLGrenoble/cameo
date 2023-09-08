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
#include "Server.h"
#include "Messages.h"
#include "JSON.h"
#include "Waiting.h"
#include "RequestSocket.h"
#include "ContextZmq.h"
#include "IdGenerator.h"
#include "../PublisherImpl.h"

namespace cameo {
namespace coms {

SubscriberZmq::SubscriberZmq() :
	m_appId{0},
	m_ended{false},
	m_canceled{false} {
}

SubscriberZmq::~SubscriberZmq() {
}

void SubscriberZmq::init(int appId, const Endpoint& endpoint, const Endpoint& appStatusEndpoint, const std::string& publisherIdentity, bool checkApp) {

	m_publisherIdentity = publisherIdentity;
	m_appId = appId;
	m_ended = false;
	m_canceled = false;

	// Create a socket for publishing.
	ContextZmq * contextImpl {dynamic_cast<ContextZmq *>(This::getCom().getContext())};
	m_subscriber.reset(new zmq::socket_t{contextImpl->getContext(), zmq::socket_type::sub});
	m_subscriber->connect(endpoint.toString());

	m_subscriber->set(zmq::sockopt::subscribe, publisherIdentity);

	// First define the cancel endpoint.
	m_cancelEndpoint = std::string{"inproc://" + IdGenerator::newStringId()};

	m_cancelPublisher = std::unique_ptr<zmq::socket_t>{new zmq::socket_t{contextImpl->getContext(), zmq::socket_type::pub}};
	m_cancelPublisher->bind(m_cancelEndpoint);

	m_subscriber->connect(m_cancelEndpoint);
	m_subscriber->set(zmq::sockopt::subscribe, message::Event::CANCEL);

	// Connect the status publisher if the app is checked.
	if (checkApp) {
		m_subscriber->connect(appStatusEndpoint.toString().c_str());
		m_subscriber->set(zmq::sockopt::subscribe, message::Event::STATUS);
	}

	// Set the poll item.
	m_items[0].socket = static_cast<void *>(*(m_subscriber.get()));
	m_items[0].fd = 0;
	m_items[0].events = ZMQ_POLLIN;
	m_items[0].revents = 0;
}

bool SubscriberZmq::sync(int timeout) {

	// Wait for timeout ms.
	int rc = zmq::poll(m_items, 1, std::chrono::milliseconds{timeout});

	// Return true if the subscriber received a message.
	return (rc != 0);
}

bool SubscriberZmq::hasEnded() const {
	return m_ended;
}

bool SubscriberZmq::isCanceled() const {
	return m_canceled;
}

std::optional<std::string> SubscriberZmq::receive() {

	while (true) {
		zmq::message_t firstPart;
		if (!m_subscriber->recv(firstPart, zmq::recv_flags::none).has_value()) {
			return {};
		}

		std::string first {static_cast<char*>(firstPart.data()), firstPart.size()};

		if (first == m_publisherIdentity) {

			zmq::message_t typePart;
			if (!m_subscriber->recv(typePart, zmq::recv_flags::none).has_value()) {
				return {};
			}

			std::string messageType {static_cast<char*>(typePart.data()), typePart.size()};

			// Get the JSON object.
			json::Object jsonType;
			json::parse(jsonType, messageType);

			int type {jsonType[message::TYPE].GetInt()};

			if (type == message::STREAM) {
				zmq::message_t dataPart;
				if (!m_subscriber->recv(dataPart, zmq::recv_flags::none).has_value()) {
					return {};
				}
				return std::string {static_cast<char*>(dataPart.data()), dataPart.size()};
			}
			else if (type == message::SYNC_STREAM) {
				// Do nothing.
			}
			else if (type == message::STREAM_END) {
				m_ended = true;
				return {};
			}
		}
		else if (first == message::Event::CANCEL) {
			return {};
		}
		else if (first == message::Event::STATUS) {
			zmq::message_t statusPart;
			if (!m_subscriber->recv(statusPart, zmq::recv_flags::none).has_value()) {
				return {};
			}

			// Get the JSON object.
			json::Object status;
			json::parse(status, statusPart);

			int id {status[message::StatusEvent::ID].GetInt()};

			if (id == m_appId) {
				State state {status[message::StatusEvent::APPLICATION_STATE].GetInt()};

				// test the terminal state
				if (state == SUCCESS
					|| state == STOPPED
					|| state == KILLED
					|| state == FAILURE) {
					// Exit because the remote application has terminated.
					return {};
				}
			}
		}
	}

	return {};
}

std::optional<std::tuple<std::string, std::string>> SubscriberZmq::receiveTwoParts() {

	while (true) {
		zmq::message_t firstPart;
		if (!m_subscriber->recv(firstPart, zmq::recv_flags::none).has_value()) {
			return {};
		}

		std::string first {static_cast<char*>(firstPart.data()), firstPart.size()};

		if (first == m_publisherIdentity) {

			zmq::message_t typePart;
			if (!m_subscriber->recv(typePart, zmq::recv_flags::none).has_value()) {
				return {};
			}

			std::string messageType {static_cast<char*>(typePart.data()), typePart.size()};

			// Get the JSON object.
			json::Object jsonType;
			json::parse(jsonType, messageType);

			int type {jsonType[message::TYPE].GetInt()};

			if (type == message::STREAM) {

				std::tuple<std::string, std::string> result;

				zmq::message_t data1Part;
				if (!m_subscriber->recv(data1Part, zmq::recv_flags::none).has_value()) {
					return {};
				}
				std::string data1 {static_cast<char*>(data1Part.data()), data1Part.size()};

				zmq::message_t data2Part;
				if (!m_subscriber->recv(data2Part, zmq::recv_flags::none).has_value()) {
					return {};
				}
				std::string data2 {static_cast<char*>(data2Part.data()), data2Part.size()};

				return std::make_tuple(data1, data2);
			}
			else if (type == message::STREAM_END) {
				m_ended = true;
				return {};
			}
		}
		else if (first == message::Event::CANCEL) {
			return {};
		}
		else if (first == message::Event::STATUS) {
			zmq::message_t statusPart;
			if (!m_subscriber->recv(statusPart, zmq::recv_flags::none).has_value()) {
				return {};
			}

			// Get the JSON object.
			json::Object status;
			json::parse(status, statusPart);

			int id {status[message::StatusEvent::ID].GetInt()};

			if (id == m_appId) {
				State state {status[message::StatusEvent::APPLICATION_STATE].GetInt()};

				// test the terminal state
				if (state == SUCCESS
					|| state == STOPPED
					|| state == KILLED
					|| state == FAILURE) {
					// Exit because the remote application has terminated.
					return {};
				}
			}
		}
	}

	return {};
}

void SubscriberZmq::cancel() {

	m_canceled = true;

	if (m_cancelPublisher) {
		std::string messageType {message::Event::CANCEL};
		zmq::message_t typePart {messageType.c_str(), messageType.length()};
		m_cancelPublisher->send(typePart, zmq::send_flags::sndmore);

		std::string data {message::Event::CANCEL};
		zmq::message_t dataPart {data.c_str(), data.length()};
		m_cancelPublisher->send(dataPart, zmq::send_flags::none);
	}
}

}
}
