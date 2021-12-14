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

PublisherZmq::PublisherZmq(const std::string& name, int numberOfSubscribers) :
	m_synchronizerPort(0),
	m_name(name),
	m_numberOfSubscribers(numberOfSubscribers),
	m_ended(false) {
}

PublisherZmq::~PublisherZmq() {
	terminate();
}

void PublisherZmq::init(int publisherPort, int synchronizerPort) {

	m_synchronizerPort = synchronizerPort;

	// create a socket for publishing
	ContextZmq* contextImpl = dynamic_cast<ContextZmq *>(application::This::getCom().getContext());
	m_publisher.reset(new zmq::socket_t(contextImpl->getContext(), ZMQ_PUB));
	std::stringstream pubEndpoint;
	pubEndpoint << "tcp://*:" << publisherPort;

	m_publisher->bind(pubEndpoint.str().c_str());
}

bool PublisherZmq::waitForSubscribers() {

	if (m_numberOfSubscribers <= 0) {
		return true;
	}

	// Create a socket to receive the messages from the subscribers.
	ContextZmq* contextImpl = dynamic_cast<ContextZmq *>(application::This::getCom().getContext());
	zmq::socket_t synchronizer(contextImpl->getContext(), ZMQ_REP);

	std::stringstream syncEndpoint;
	std::string url = "tcp://*";

	syncEndpoint << url << ":" << m_synchronizerPort;
	synchronizer.bind(syncEndpoint.str().c_str());

	// Loop until the number of subscribers is reached.
	int counter = 0;
	bool canceled = false;

	while (counter < m_numberOfSubscribers) {

		std::unique_ptr<zmq::message_t> message(new zmq::message_t);
		synchronizer.recv(message.get(), 0);

		// Get the JSON request.
		json::Object request;
		json::parse(request, message.get());

		int type = request[message::TYPE].GetInt();

		std::unique_ptr<zmq::message_t> reply;

		if (type == message::SYNC) {
			reply.reset(responseToSyncRequest());
		}
		else if (type == message::SUBSCRIBE_PUBLISHER_v0) {
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
			synchronizer.send(*reply);
		}
	}

	return !canceled;
}

void PublisherZmq::cancelWaitForSubscribers() {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushInt(message::CANCEL);

	// Create a request socket only for the request.
	std::unique_ptr<RequestSocket> requestSocket = application::This::getCom().createRequestSocket(application::This::getEndpoint().withPort(m_synchronizerPort).toString());
	requestSocket->requestJSON(request.toString());
}

void PublisherZmq::sendBinary(const std::string& data) {

	// send a STREAM message by the publisher socket
	publish(message::Event::STREAM, data.c_str(), data.length());
}

void PublisherZmq::send(const std::string& data) {

	// encode the data
	std::string result;
	serialize(data, result);

	// send a STREAM message by the publisher socket
	publish(message::Event::STREAM, result.c_str(), result.length());
}

void PublisherZmq::sendTwoBinaryParts(const std::string& data1, const std::string& data2) {

	// send a STREAM message by the publisher socket
	publishTwoParts(message::Event::STREAM, data1.c_str(), data1.length(), data2.c_str(), data2.length());
}

void PublisherZmq::setEnd() {

	if (!m_ended && m_publisher.get() != nullptr) {
		// send a dummy ENDSTREAM message by the publisher socket
		std::string data(message::Event::ENDSTREAM);
		publish(message::Event::ENDSTREAM, data.c_str(), data.length());

		m_ended = true;
	}
}

bool PublisherZmq::isEnded() {
	return m_ended;
}

void PublisherZmq::terminate() {

	if (m_publisher.get() != nullptr) {
		setEnd();
		m_publisher.reset(nullptr);

		json::Object response = application::This::getCom().requestJSON(createTerminatePublisherRequest(application::This::getId(), m_name));

		int value = response[message::RequestResponse::VALUE].GetInt();
		bool success = (value != -1);

		if (!success) {
			std::cerr << "server cannot destroy publisher " << m_name << std::endl;
		}
	}
}

void PublisherZmq::publish(const std::string& header, const char* data, std::size_t size) {

	zmq::message_t requestType(header.length());
	memcpy(requestType.data(), header.c_str(), header.length());

	zmq::message_t requestData(size);
	memcpy(requestData.data(), data, size);

	m_publisher->send(requestType, ZMQ_SNDMORE);
	m_publisher->send(requestData);
}

void PublisherZmq::publishTwoParts(const std::string& header, const char* data1, std::size_t size1, const char* data2, std::size_t size2) {

	zmq::message_t requestType(header.length());
	memcpy(requestType.data(), header.c_str(), header.length());

	zmq::message_t requestData1(size1);
	memcpy(requestData1.data(), data1, size1);

	zmq::message_t requestData2(size2);
	memcpy(requestData2.data(), data2, size2);

	m_publisher->send(requestType, ZMQ_SNDMORE);
	m_publisher->send(requestData1, ZMQ_SNDMORE);
	m_publisher->send(requestData2);
}

zmq::message_t * PublisherZmq::responseToSyncRequest() {

	// send a dummy SYNC message by the publisher socket
	std::string data(message::Event::SYNC);
	publish(message::Event::SYNC, data.c_str(), data.length());

	std::string result = createRequestResponse(0, "OK");

	size_t size = result.length();
	zmq::message_t * reply = new zmq::message_t(size);
	memcpy((void *) reply->data(), result.c_str(), size);

	return reply;
}

zmq::message_t * PublisherZmq::responseToSubscribeRequest() {

	std::string result = createRequestResponse(0, "OK");

	zmq::message_t * reply = new zmq::message_t(result.length());
	memcpy(reply->data(), result.c_str(), result.length());

	return reply;
}

zmq::message_t * PublisherZmq::responseToCancelRequest() {

	std::string result = createRequestResponse(0, "OK");

	zmq::message_t * reply = new zmq::message_t(result.length());
	memcpy(reply->data(), result.c_str(), result.length());

	return reply;
}

zmq::message_t * PublisherZmq::responseToUnknownRequest() {

	std::string result = createRequestResponse(-1, "Unknown command");

	zmq::message_t * reply = new zmq::message_t(result.length());
	memcpy(reply->data(), result.c_str(), result.length());

	return reply;
}

std::string PublisherZmq::createTerminatePublisherRequest(int id, const std::string& name) const {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushInt(message::TERMINATE_PUBLISHER_v0);

	request.pushKey(message::TerminatePublisherRequest::ID);
	request.pushInt(id);

	request.pushKey(message::TerminatePublisherRequest::NAME);
	request.pushString(name);

	return request.toString();
}

}
}

