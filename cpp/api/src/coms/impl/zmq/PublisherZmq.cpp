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

#include "PublisherZmq.h"

#include "This.h"
#include "Messages.h"
#include "RequestSocket.h"
#include "ContextZmq.h"
#include "../../../base/JSON.h"

namespace cameo {
namespace coms {

PublisherZmq::PublisherZmq() :
	m_publisherPort{0},
	m_ended{false} {
}

PublisherZmq::~PublisherZmq() {
	terminate();
}

void PublisherZmq::init(const std::string& publisherIdentity) {

	m_publisherIdentity = publisherIdentity;

	// Create a socket for publishing.
	ContextZmq * contextImpl {dynamic_cast<ContextZmq *>(This::getCom().getContext())};
	m_publisher.reset(new zmq::socket_t{contextImpl->getContext(), zmq::socket_type::pub});

	// Connect to the proxy.
	Endpoint subscriberProxyEndpoint {This::getEndpoint().withPort(This::getCom().getSubscriberProxyPort())};
	m_publisher->connect(subscriberProxyEndpoint.toString());

	std::string endpointPrefix {"tcp://*:"};

	// Loop to find an available port for the publisher.
	while (true) {

		int port {This::getCom().requestPort()};
		std::string pubEndpoint {endpointPrefix + std::to_string(port)};

		try {
			m_publisher->bind(pubEndpoint.c_str());
			m_publisherPort = port;
			break;
		}
		catch (...) {
			This::getCom().setPortUnavailable(port);
		}
	}
}

int PublisherZmq::getPublisherPort() const {
	return m_publisherPort;
}

void PublisherZmq::sendSync() {

	// send a SYNC_STREAM message by the publisher socket
	zmq::message_t identityPart {m_publisherIdentity.c_str(), m_publisherIdentity.length()};
	m_publisher->send(identityPart, zmq::send_flags::sndmore);

	std::string messageType {createMessageType(message::SYNC_STREAM)};
	zmq::message_t typePart {messageType.c_str(), messageType.length()};
	m_publisher->send(typePart, zmq::send_flags::none);
}

void PublisherZmq::send(const std::string& data) {

	// send a STREAM message by the publisher socket
	zmq::message_t identityPart {m_publisherIdentity.c_str(), m_publisherIdentity.length()};
	m_publisher->send(identityPart, zmq::send_flags::sndmore);

	std::string messageType {createMessageType(message::STREAM)};
	zmq::message_t typePart {messageType.c_str(), messageType.length()};
	m_publisher->send(typePart, zmq::send_flags::sndmore);

	zmq::message_t dataPart {data.c_str(), data.length()};
	m_publisher->send(dataPart, zmq::send_flags::none);
}

void PublisherZmq::sendTwoParts(const std::string& data1, const std::string& data2) {

	// send a STREAM message by the publisher socket
	zmq::message_t identityPart {m_publisherIdentity.c_str(), m_publisherIdentity.length()};
	m_publisher->send(identityPart, zmq::send_flags::sndmore);

	std::string messageType {createMessageType(message::STREAM)};
	zmq::message_t typePart {messageType.c_str(), messageType.length()};
	m_publisher->send(typePart, zmq::send_flags::sndmore);

	zmq::message_t data1Part {data1.c_str(), data1.length()};
	m_publisher->send(data1Part, zmq::send_flags::sndmore);

	zmq::message_t data2Part {data2.c_str(), data2.length()};
	m_publisher->send(data2Part, zmq::send_flags::none);
}

void PublisherZmq::setEnd() {

	if (!m_ended && m_publisher) {
		// send a STREAM_END message by the publisher socket
		zmq::message_t identityPart {m_publisherIdentity.c_str(), m_publisherIdentity.length()};
		m_publisher->send(identityPart, zmq::send_flags::sndmore);

		std::string messageType {createMessageType(message::STREAM_END)};
		zmq::message_t typePart {messageType.c_str(), messageType.length()};
		m_publisher->send(typePart, zmq::send_flags::none);

		m_ended = true;
	}
}

bool PublisherZmq::hasEnded() {
	return m_ended;
}

void PublisherZmq::terminate() {

	if (m_publisher) {
		setEnd();
		m_publisher.reset(nullptr);

		// Release the publisher port.
		This::getCom().releasePort(m_publisherPort);
	}
}

std::string PublisherZmq::createMessageType(int type) {

	json::StringObject jsonType;
	jsonType.pushKey(message::TYPE);
	jsonType.pushValue(type);

	return jsonType.dump();
}

}
}

