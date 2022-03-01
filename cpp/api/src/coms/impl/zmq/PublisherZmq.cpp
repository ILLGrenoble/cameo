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
#include "Application.h"
#include "Messages.h"
#include "JSON.h"
#include "../../../base/impl/zmq/ContextZmq.h"
#include "../../../base/RequestSocket.h"
#include <sstream>


namespace cameo {
namespace coms {

PublisherZmq::PublisherZmq() :
	m_publisherPort(0),
	m_ended(false) {
}

PublisherZmq::~PublisherZmq() {
	terminate();
}

void PublisherZmq::init(const std::string& publisherIdentity) {

	m_publisherIdentity = publisherIdentity;

	// Create a socket for publishing.
	ContextZmq* contextImpl = dynamic_cast<ContextZmq *>(application::This::getCom().getContext());
	m_publisher.reset(new zmq::socket_t(contextImpl->getContext(), zmq::socket_type::pub));

	// Connect to the proxy.
	Endpoint subscriberProxyEndpoint = application::This::getEndpoint().withPort(application::This::getCom().getSubscriberProxyPort());
	m_publisher->connect(subscriberProxyEndpoint.toString());

	std::string endpointPrefix("tcp://*:");

	// Loop to find an available port for the publisher.
	while (true) {

		int port = application::This::getCom().requestPort();
		std::string pubEndpoint = endpointPrefix + std::to_string(port);

		try {
			m_publisher->bind(pubEndpoint.c_str());
			m_publisherPort = port;
			break;
		}
		catch (...) {
			application::This::getCom().setPortUnavailable(port);
		}
	}
}

int PublisherZmq::getPublisherPort() const {
	return m_publisherPort;
}

void PublisherZmq::sendBinary(const std::string& data) {

	// send a STREAM message by the publisher socket
	publish(data.c_str(), data.length());
}

void PublisherZmq::send(const std::string& data) {

	// send a STREAM message by the publisher socket
	publish(data.c_str(), data.length());
}

void PublisherZmq::sendTwoBinaryParts(const std::string& data1, const std::string& data2) {

	// send a STREAM message by the publisher socket
	publishTwoParts(data1.c_str(), data1.length(), data2.c_str(), data2.length());
}

void PublisherZmq::setEnd() {

	if (!m_ended && m_publisher) {
		// send a STREAM_END message by the publisher socket
		zmq::message_t identityPart(m_publisherIdentity.c_str(), m_publisherIdentity.length());
		m_publisher->send(identityPart, zmq::send_flags::sndmore);

		std::string messageType = createMessageType(message::STREAM_END);
		zmq::message_t typePart(messageType.c_str(), messageType.length());
		m_publisher->send(typePart, zmq::send_flags::none);

		m_ended = true;
	}
}

bool PublisherZmq::isEnded() {
	return m_ended;
}

void PublisherZmq::terminate() {

	if (m_publisher) {
		setEnd();
		m_publisher.reset(nullptr);

		// Release the publisher port.
		application::This::getCom().releasePort(m_publisherPort);
	}
}

std::string PublisherZmq::createMessageType(int type) {

	json::StringObject jsonType;
	jsonType.pushKey(message::TYPE);
	jsonType.pushValue(type);

	return jsonType.toString();
}

void PublisherZmq::publish(const char* data, std::size_t size) {

	zmq::message_t identityPart(m_publisherIdentity.c_str(), m_publisherIdentity.length());
	m_publisher->send(identityPart, zmq::send_flags::sndmore);

	std::string messageType = createMessageType(message::STREAM);
	zmq::message_t typePart(messageType.c_str(), messageType.length());
	m_publisher->send(typePart, zmq::send_flags::sndmore);

	zmq::message_t dataPart(data, size);
	m_publisher->send(dataPart, zmq::send_flags::none);
}

void PublisherZmq::publishTwoParts(const char* data1, std::size_t size1, const char* data2, std::size_t size2) {

	zmq::message_t identityPart(m_publisherIdentity.c_str(), m_publisherIdentity.length());
	m_publisher->send(identityPart, zmq::send_flags::sndmore);

	std::string messageType = createMessageType(message::STREAM);
	zmq::message_t typePart(messageType.c_str(), messageType.length());
	m_publisher->send(typePart, zmq::send_flags::sndmore);

	zmq::message_t data1Part(data1, size1);
	m_publisher->send(data1Part, zmq::send_flags::sndmore);

	zmq::message_t data2Part(data2, size2);
	m_publisher->send(data2Part, zmq::send_flags::none);
}

}
}

