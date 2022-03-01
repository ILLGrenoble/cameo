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
	m_appId(0),
	m_ended(false),
	m_canceled(false) {
}

SubscriberZmq::~SubscriberZmq() {
}

void SubscriberZmq::init(int appId, const Endpoint& endpoint, const Endpoint& appStatusEndpoint, const std::string& publisherIdentity) {

	m_publisherIdentity = publisherIdentity;
	m_appId = appId;
	m_ended = false;
	m_canceled = false;

	// Create a socket for publishing.
	ContextZmq* contextImpl = dynamic_cast<ContextZmq *>(application::This::getCom().getContext());
	m_subscriber.reset(new zmq::socket_t(contextImpl->getContext(), zmq::socket_type::sub));
	m_subscriber->connect(endpoint.toString());

	m_subscriber->setsockopt(ZMQ_SUBSCRIBE, publisherIdentity.c_str(), publisherIdentity.length());


	// We must first bind the cancel publisher before connecting the subscriber.
	std::stringstream cancelEndpoint;

	// We define a unique name.
	cancelEndpoint << "inproc://cancel." << CancelIdGenerator::newId();
	m_cancelEndpoint = cancelEndpoint.str();

	m_cancelPublisher = std::unique_ptr<zmq::socket_t>(new zmq::socket_t(contextImpl->getContext(), zmq::socket_type::pub));
	m_cancelPublisher->bind(m_cancelEndpoint.c_str());

	m_subscriber->connect(m_cancelEndpoint.c_str());
	m_subscriber->setsockopt(ZMQ_SUBSCRIBE, message::Event::CANCEL, std::string(message::Event::CANCEL).length());

	m_subscriber->connect(appStatusEndpoint.toString().c_str());
	m_subscriber->setsockopt(ZMQ_SUBSCRIBE, message::Event::STATUS, std::string(message::Event::STATUS).length());
}

bool SubscriberZmq::isEnded() const {
	return m_ended;
}

bool SubscriberZmq::isCanceled() const {
	return m_canceled;
}

std::optional<std::string> SubscriberZmq::receiveBinary() {

	while (true) {
		zmq::message_t firstPart;
		if (!m_subscriber->recv(firstPart, zmq::recv_flags::none).has_value()) {
			return {};
		}

		std::string first(static_cast<char*>(firstPart.data()), firstPart.size());

		if (first == m_publisherIdentity) {

			zmq::message_t typePart;
			if (!m_subscriber->recv(typePart, zmq::recv_flags::none).has_value()) {
				return {};
			}

			std::string messageType(static_cast<char*>(typePart.data()), typePart.size());

			// Get the JSON object.
			json::Object jsonType;
			json::parse(jsonType, messageType);

			int type = jsonType[message::TYPE].GetInt();

			if (type == message::STREAM) {
				zmq::message_t dataPart;
				if (!m_subscriber->recv(dataPart, zmq::recv_flags::none).has_value()) {
					return {};
				}
				return std::string(static_cast<char*>(dataPart.data()), dataPart.size());
			}
			else if (type == message::STREAM_END) {
				m_ended = true;
				return {};
			}
		}
		else if (first == message::Event::CANCEL) {
			m_canceled = true;
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
		zmq::message_t firstPart;
		if (!m_subscriber->recv(firstPart, zmq::recv_flags::none).has_value()) {
			return {};
		}

		std::string first(static_cast<char*>(firstPart.data()), firstPart.size());

		if (first == m_publisherIdentity) {

			zmq::message_t typePart;
			if (!m_subscriber->recv(typePart, zmq::recv_flags::none).has_value()) {
				return {};
			}

			std::string messageType(static_cast<char*>(typePart.data()), typePart.size());

			// Get the JSON object.
			json::Object jsonType;
			json::parse(jsonType, messageType);

			int type = jsonType[message::TYPE].GetInt();

			if (type == message::STREAM) {

				std::tuple<std::string, std::string> result;

				zmq::message_t data1Part;
				if (!m_subscriber->recv(data1Part, zmq::recv_flags::none).has_value()) {
					return {};
				}
				std::string data1 = std::string(static_cast<char*>(data1Part.data()), data1Part.size());

				zmq::message_t data2Part;
				if (!m_subscriber->recv(data2Part, zmq::recv_flags::none).has_value()) {
					return {};
				}
				std::string data2 = std::string(static_cast<char*>(data2Part.data()), data2Part.size());

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

	std::string messageType(message::Event::CANCEL);
	zmq::message_t typePart(messageType.c_str(), messageType.length());
	m_cancelPublisher->send(typePart, zmq::send_flags::sndmore);

	std::string data(message::Event::CANCEL);
	zmq::message_t dataPart(data.c_str(), data.length());
	m_cancelPublisher->send(dataPart, zmq::send_flags::none);
}

}
}
