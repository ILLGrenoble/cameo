/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

#include "SubscriberZmq.h"

#include "This.h"
#include "Messages.h"
#include "Waiting.h"
#include "RequestSocket.h"
#include "ContextZmq.h"
#include "IdGenerator.h"
#include "../PublisherImpl.h"
#include "../../../base/JSON.h"

namespace cameo {
namespace coms {

SubscriberZmq::SubscriberZmq() :
	m_appId{0},
	m_pollingTime{100},
	m_timeout{0},
	m_ended{false},
	m_canceled{false},
	m_timedout{false} {
}

SubscriberZmq::~SubscriberZmq() {
}

void SubscriberZmq::setPollingTime(int value) {
	m_pollingTime = value;
}

void SubscriberZmq::setTimeout(int value) {
	m_timeout = value;
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

bool SubscriberZmq::hasTimedout() {
	return m_timedout;
}

bool SubscriberZmq::receiveMessage(zmq::message_t& message) {

	// Define the number of iterations.
	int n {0};
	if (m_pollingTime > 0) {
		n = m_timeout / m_pollingTime + 1;
	}

	// Create the poller.
	zmq::pollitem_t items[] = {
		{ *m_subscriber.get(), 0, ZMQ_POLLIN, 0 }
	};

	// Infinite loop if timeout is 0 or finite loop if timeout is defined.
	int i {0};
	while (i < n || m_timeout == 0) {

		// Check if the requester has been canceled.
		if (m_canceled) {
			return false;
		}

		// Poll the requester.
		zmq::poll(&items[0], 1, std::chrono::milliseconds{m_pollingTime});

		// Get a reply.
		if (items[0].revents & ZMQ_POLLIN) {
			if (!m_subscriber->recv(message, zmq::recv_flags::none).has_value()) {
				return false;
			}
			return true;
		}

		i++;
	}

	// Timeout.
	m_timedout.store(true);

	// No need to reset the socket after a timeout.

	return false;
}

std::optional<std::string> SubscriberZmq::receive() {

	// Reset timedout.
	m_timedout.store(false);

	while (true) {
		zmq::message_t firstPart;
		if (!receiveMessage(firstPart)) {
			return {};
		}

		std::string first {static_cast<char*>(firstPart.data()), firstPart.size()};

		if (first == m_publisherIdentity) {

			zmq::message_t typePart;
			if (!receiveMessage(typePart)) {
				return {};
			}

			std::string messageType {static_cast<char*>(typePart.data()), typePart.size()};

			// Get the JSON object.
			json::Object jsonType;
			json::parse(jsonType, messageType);

			int type {jsonType[message::TYPE].GetInt()};

			if (type == message::STREAM) {
				zmq::message_t dataPart;
				if (!receiveMessage(dataPart)) {
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
			if (!receiveMessage(statusPart)) {
				return {};
			}

			// Get the JSON object.
			json::Object status;
			json::parse(status, statusPart);

			int id {status[message::StatusEvent::ID].GetInt()};

			if (id == m_appId) {
				state::Value state {status[message::StatusEvent::APPLICATION_STATE].GetInt()};

				// test the terminal state
				if (state == state::SUCCESS
					|| state == state::STOPPED
					|| state == state::KILLED
					|| state == state::FAILURE) {
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
		if (!receiveMessage(firstPart)) {
			return {};
		}

		std::string first {static_cast<char*>(firstPart.data()), firstPart.size()};

		if (first == m_publisherIdentity) {

			zmq::message_t typePart;
			if (!receiveMessage(typePart)) {
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
				if (!receiveMessage(data1Part)) {
					return {};
				}
				std::string data1 {static_cast<char*>(data1Part.data()), data1Part.size()};

				zmq::message_t data2Part;
				if (!receiveMessage(data2Part)) {
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
			if (!receiveMessage(statusPart)) {
				return {};
			}

			// Get the JSON object.
			json::Object status;
			json::parse(status, statusPart);

			int id {status[message::StatusEvent::ID].GetInt()};

			if (id == m_appId) {
				state::Value state {status[message::StatusEvent::APPLICATION_STATE].GetInt()};

				// test the terminal state
				if (state == state::SUCCESS
					|| state == state::STOPPED
					|| state == state::KILLED
					|| state == state::FAILURE) {
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