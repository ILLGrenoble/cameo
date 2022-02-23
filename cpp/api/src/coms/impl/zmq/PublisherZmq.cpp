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
#include "Serializer.h"
#include "JSON.h"
#include "../../../base/impl/zmq/ContextZmq.h"
#include "../../../base/Messages.h"
#include "../../../base/RequestSocket.h"
#include <sstream>

namespace cameo {
namespace coms {

PublisherZmq::PublisherZmq() :
	m_publisherPort(0),
	m_synchronizerPort(0),
	m_numberOfSubscribers(0),
	m_ended(false) {
}

PublisherZmq::~PublisherZmq() {
	terminate();
}

void PublisherZmq::init(const std::string& publisherIdentity, int numberOfSubscribers) {

	m_publisherIdentity = publisherIdentity;
	m_numberOfSubscribers = numberOfSubscribers;

	// Create a socket for publishing.
	ContextZmq* contextImpl = dynamic_cast<ContextZmq *>(application::This::getCom().getContext());
	m_publisher.reset(new zmq::socket_t(contextImpl->getContext(), zmq::socket_type::pub));

	// Connect to the proxy.
	Endpoint subscriberProxyEndpoint = application::This::getEndpoint().withPort(application::This::getCom().getSubscriberProxyPort());
	m_publisher->connect(subscriberProxyEndpoint.toString());

	std::cout << "Connected publisher to " << subscriberProxyEndpoint.toString() << std::endl;

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

	// Define the synchronizer if the number of subscribers is strictly positive.
	if (m_numberOfSubscribers > 0) {

		m_synchronizer.reset(new zmq::socket_t(contextImpl->getContext(), zmq::socket_type::rep));

		// Loop to find an available port for the synchronizer.
		while (true) {

			int port = application::This::getCom().requestPort();
			std::string syncEndpoint = endpointPrefix + std::to_string(port);

			try {
				m_synchronizer->bind(syncEndpoint.c_str());
				m_synchronizerPort = port;
				break;
			}
			catch (...) {
				application::This::getCom().setPortUnavailable(port);
			}
		}
	}
}

int PublisherZmq::getPublisherPort() const {
	return m_publisherPort;
}

int PublisherZmq::getSynchronizerPort() const {
	return m_synchronizerPort;
}

bool PublisherZmq::waitForSubscribers() {

	if (m_numberOfSubscribers <= 0) {
		return true;
	}

	// Loop until the number of subscribers is reached.
	int counter = 0;
	bool canceled = false;

	while (counter < m_numberOfSubscribers) {

		zmq::message_t message;
		if (!m_synchronizer->recv(message, zmq::recv_flags::none).has_value()) {
			return false;
		}

		// Get the JSON request.
		json::Object request;
		json::parse(request, message);

		int type = request[message::TYPE].GetInt();

		std::unique_ptr<zmq::message_t> reply;

		if (type == message::SYNC) {
			reply.reset(responseToSyncRequest());
		}
		else if (type == PublisherImpl::SUBSCRIBE_PUBLISHER) {
			counter++;
			reply.reset(responseToSubscribeRequest());
		}
		else if (type == message::CANCEL) {
			canceled = true;
			counter = m_numberOfSubscribers;
			reply.reset(responseToCancelRequest());
		}
		else {
			reply.reset(responseToUnknownRequest());
		}

		// send to the client
		if (reply != nullptr) {
			m_synchronizer->send(*reply, zmq::send_flags::none);
		}
	}

	return !canceled;
}

void PublisherZmq::cancelWaitForSubscribers() {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushValue(message::CANCEL);

	// Create a request socket only for the request.
	std::unique_ptr<RequestSocket> requestSocket = application::This::getCom().createRequestSocket(application::This::getEndpoint().withPort(m_synchronizerPort).toString(), "zzzZZZ");
	requestSocket->requestJSON(request.toString());
}

void PublisherZmq::sendBinary(const std::string& data) {

	// send a STREAM message by the publisher socket
	publish(data.c_str(), data.length());
}

void PublisherZmq::send(const std::string& data) {

	// encode the data
	std::string result;
	serialize(data, result);

	// send a STREAM message by the publisher socket
	publish(result.c_str(), result.length());
}

void PublisherZmq::sendTwoBinaryParts(const std::string& data1, const std::string& data2) {

	// send a STREAM message by the publisher socket
	publishTwoParts(data1.c_str(), data1.length(), data2.c_str(), data2.length());
}

void PublisherZmq::setEnd() {

	if (!m_ended && m_publisher) {
		// send a STREAM_END message by the publisher socket
		zmq::message_t requestIdentity(m_publisherIdentity.c_str(), m_publisherIdentity.length());
		m_publisher->send(requestIdentity, zmq::send_flags::sndmore);

		std::string type = createMessageType(message::STREAM_END);
		zmq::message_t requestType(type.c_str(), type.length());
		m_publisher->send(requestType, zmq::send_flags::none);

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

	if (m_synchronizer) {
		m_synchronizer.reset(nullptr);

		// Release the synchronizer port.
		application::This::getCom().releasePort(m_synchronizerPort);
	}
}

std::string PublisherZmq::createMessageType(int type) {

	json::StringObject messageType;
	messageType.pushKey(message::TYPE);
	messageType.pushValue(type);

	return messageType.toString();
}

void PublisherZmq::publish(const char* data, std::size_t size) {

	zmq::message_t requestIdentity(m_publisherIdentity.c_str(), m_publisherIdentity.length());
	m_publisher->send(requestIdentity, zmq::send_flags::sndmore);

	std::string type = createMessageType(message::STREAM);
	zmq::message_t requestType(type.c_str(), type.length());
	m_publisher->send(requestType, zmq::send_flags::sndmore);

	zmq::message_t requestData(data, size);
	m_publisher->send(requestData, zmq::send_flags::none);
}

void PublisherZmq::publishTwoParts(const char* data1, std::size_t size1, const char* data2, std::size_t size2) {

	zmq::message_t requestIdentity(m_publisherIdentity.c_str(), m_publisherIdentity.length());
	m_publisher->send(requestIdentity, zmq::send_flags::sndmore);

	std::string type = createMessageType(message::STREAM);
	zmq::message_t requestType(type.c_str(), type.length());
	m_publisher->send(requestType, zmq::send_flags::sndmore);

	zmq::message_t requestData1(data1, size1);
	m_publisher->send(requestData1, zmq::send_flags::sndmore);

	zmq::message_t requestData2(data2, size2);
	m_publisher->send(requestData2, zmq::send_flags::none);
}

zmq::message_t * PublisherZmq::responseToSyncRequest() {

	// Send a dummy SYNC message by the publisher socket.
	zmq::message_t requestIdentity(m_publisherIdentity.c_str(), m_publisherIdentity.length());
	m_publisher->send(requestIdentity, zmq::send_flags::sndmore);

	std::string type = createMessageType(message::SYNC);
	zmq::message_t requestType(type.c_str(), type.length());
	m_publisher->send(requestType, zmq::send_flags::none);

	std::string result = createRequestResponse(0, "OK");

	size_t size = result.length();
	zmq::message_t * reply = new zmq::message_t(result.c_str(), size);

	return reply;
}

zmq::message_t * PublisherZmq::responseToSubscribeRequest() {

	std::string result = createRequestResponse(0, "OK");

	zmq::message_t * reply = new zmq::message_t(result.c_str(), result.length());

	return reply;
}

zmq::message_t * PublisherZmq::responseToCancelRequest() {

	std::string result = createRequestResponse(0, "OK");

	zmq::message_t * reply = new zmq::message_t(result.c_str(), result.length());

	return reply;
}

zmq::message_t * PublisherZmq::responseToUnknownRequest() {

	std::string result = createRequestResponse(-1, "Unknown command");

	zmq::message_t * reply = new zmq::message_t(result.c_str(), result.length());

	return reply;
}

}
}

