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

#include "PublisherImpl.h"

#include <boost/bind.hpp>
#include <sstream>
#include "../Application.h"
#include "../Serializer.h"
#include "ApplicationImpl.h"

using namespace std;
using namespace boost;

namespace cameo {

const std::string PublisherImpl::SYNC = "SYNC";
const std::string PublisherImpl::STREAM = "STREAM";
const std::string PublisherImpl::ENDSTREAM = "ENDSTREAM";

PublisherImpl::PublisherImpl(const application::This * application, int publisherPort, int synchronizerPort, const std::string& name, int numberOfSubscribers) :
	m_application(application),
	m_publisherPort(publisherPort),
	m_synchronizerPort(synchronizerPort),
	m_name(name),
	m_numberOfSubscribers(numberOfSubscribers),
	m_ended(false) {

	// create a socket for publishing
	m_publisher.reset(new zmq::socket_t(m_application->m_impl->m_context, ZMQ_PUB));
	stringstream pubEndpoint;
	pubEndpoint << "tcp://*:" << m_publisherPort;

	m_publisher->bind(pubEndpoint.str().c_str());
}

PublisherImpl::~PublisherImpl() {
	terminate();
}

const std::string& PublisherImpl::getName() const {
	return m_name;
}

const std::string& PublisherImpl::getApplicationName() const {
	return m_application->getName();
}

int PublisherImpl::getApplicationId() const {
	return m_application->getId();
}

const std::string& PublisherImpl::getApplicationEndpoint() const {
	return m_application->getEndpoint();
}

bool PublisherImpl::waitForSubscribers() {

	if (m_numberOfSubscribers <= 0) {
		return true;
	}

	// create a socket to receive the messages from the subscribers
	zmq::socket_t synchronizer(m_application->m_impl->m_context, ZMQ_REP);

	stringstream syncEndpoint;
	string url = "tcp://*";

	syncEndpoint << url << ":" << m_synchronizerPort;
	synchronizer.bind(syncEndpoint.str().c_str());

	// loop until the number of subscribers is reached
	int counter = 0;
	bool canceled = false;

	while (counter < m_numberOfSubscribers) {

		zmq::message_t * message = new zmq::message_t;
		synchronizer.recv(message, 0);

		// multi-part message, first part is the type
		proto::MessageType messageType;
		messageType.ParseFromArray((*message).data(), (*message).size());

		if (message->more()) {
			delete message;
			message = new zmq::message_t;
			synchronizer.recv(message, 0);

		} else {
			cerr << "unexpected number of frames, should be 2" << endl;
			continue;
		}

		zmq::message_t * reply = 0;

		if (messageType.type() == proto::MessageType_Type_INIT) {
			reply = processInitCommand();

		} else if (messageType.type() == proto::MessageType_Type_SUBSCRIBEPUBLISHER) {
			counter++;
			reply = processSubscribePublisherCommand();

		} else if (messageType.type() == proto::MessageType_Type_CANCEL) {
			canceled = true;
			counter = m_numberOfSubscribers;
			reply = processCancelPublisherSyncCommand();

		} else {
			cerr << "unknown message type " << messageType.type() << endl;
			synchronizer.send(*message);
		}

		// send to the client
		if (reply != 0) {
			synchronizer.send(*reply);
		}

		delete reply;
		delete message;
	}

	return !canceled;
}

void PublisherImpl::cancelWaitForSubscribers() {

	stringstream endpoint;
	endpoint << m_application->getUrl() << ":" << (m_publisherPort + 1);

	string strRequestType = m_application->m_impl->createRequest(PROTO_CANCEL);
	string strRequestData;

	proto::CancelPublisherSyncCommand cancelPublisherSyncCommand;
	cancelPublisherSyncCommand.SerializeToString(&strRequestData);

	zmq::message_t* reply = m_application->m_impl->tryRequestWithOnePartReply(strRequestType, strRequestData, endpoint.str());

	proto::RequestResponse requestResponse;
	requestResponse.ParseFromArray((*reply).data(), (*reply).size());
	delete reply;
}

WaitingImpl * PublisherImpl::waiting() {

	return new GenericWaitingImpl(bind(&PublisherImpl::cancelWaitForSubscribers, this));
}

void PublisherImpl::sendBinary(const std::string& data) {

	// send a STREAM message by the publisher socket
	publish(STREAM, data.c_str(), data.length());
}

void PublisherImpl::send(const std::string& data) {

	// encode the data
	string result;
	serialize(data, result);

	// send a STREAM message by the publisher socket
	publish(STREAM, result.c_str(), result.length());
}

void PublisherImpl::send(const int32_t* data, std::size_t size) {

	// encode the data
	string result;
	serialize(data, size, result);

	// send a STREAM message by the publisher socket
	publish(STREAM, result.c_str(), result.length());
}

void PublisherImpl::send(const int64_t* data, std::size_t size) {

	// encode the data
	string result;
	serialize(data, size, result);

	// send a STREAM message by the publisher socket
	publish(STREAM, result.c_str(), result.length());
}

void PublisherImpl::send(const float* data, std::size_t size) {

	// encode the data
	string result;
	serialize(data, size, result);

	// send a STREAM message by the publisher socket
	publish(STREAM, result.c_str(), result.length());
}

void PublisherImpl::send(const double* data, std::size_t size) {

	// encode the data
	string result;
	serialize(data, size, result);

	// send a STREAM message by the publisher socket
	publish(STREAM, result.c_str(), result.length());
}

void PublisherImpl::sendTwoBinaryParts(const std::string& data1, const std::string& data2) {

	// send a STREAM message by the publisher socket
	publishTwoParts(STREAM, data1.c_str(), data1.length(), data2.c_str(), data2.length());
}

void PublisherImpl::setEnd() {

	if (!m_ended && m_publisher.get() != 0) {
		// send a dummy ENDSTREAM message by the publisher socket
		string data = "endstream";
		publish(ENDSTREAM, data.c_str(), data.length());

		m_ended = true;
	}
}

bool PublisherImpl::hasEnded() {
	return m_ended;
}

void PublisherImpl::terminate() {

	if (m_publisher.get() != 0) {
		setEnd();
		m_publisher.reset(0);

		bool success = m_application->destroyPublisher(m_name);
		if (!success) {
			cerr << "server cannot destroy publisher " << m_name << endl;
		}
	}
}

void PublisherImpl::publish(const std::string& header, const char* data, std::size_t size) {

	zmq::message_t requestType(header.length());
	memcpy((void *) requestType.data(), header.c_str(), header.length());

	zmq::message_t requestData(size);
	memcpy((void *) requestData.data(), data, size);

	m_publisher->send(requestType, ZMQ_SNDMORE);
	m_publisher->send(requestData);
}

void PublisherImpl::publishTwoParts(const std::string& header, const char* data1, std::size_t size1, const char* data2, std::size_t size2) {

	zmq::message_t requestType(header.length());
	memcpy((void *) requestType.data(), header.c_str(), header.length());

	zmq::message_t requestData1(size1);
	memcpy((void *) requestData1.data(), data1, size1);

	zmq::message_t requestData2(size2);
	memcpy((void *) requestData2.data(), data2, size2);

	m_publisher->send(requestType, ZMQ_SNDMORE);
	m_publisher->send(requestData1, ZMQ_SNDMORE);
	m_publisher->send(requestData2);
}

zmq::message_t * PublisherImpl::processInitCommand() {

	// send a dummy SYNC message by the publisher socket
	string data = "sync";
	publish(SYNC, data.c_str(), data.length());

	data = "Connection OK";
	size_t size = data.length();
	zmq::message_t * reply = new zmq::message_t(size);
	memcpy((void *) reply->data(), data.c_str(), size);

	return reply;
}

zmq::message_t * PublisherImpl::processSubscribePublisherCommand() {

	proto::RequestResponse requestResponse;
	requestResponse.set_value(0);
	requestResponse.set_message("OK");

	string result = requestResponse.SerializeAsString();

	zmq::message_t * reply = new zmq::message_t(result.length());
	memcpy((void *) reply->data(), result.c_str(), result.length());

	return reply;
}

zmq::message_t * PublisherImpl::processCancelPublisherSyncCommand() {

	proto::RequestResponse requestResponse;
	requestResponse.set_value(0);
	requestResponse.set_message("OK");

	string result = requestResponse.SerializeAsString();

	zmq::message_t * reply = new zmq::message_t(result.length());
	memcpy((void *) reply->data(), result.c_str(), result.length());

	return reply;
}


}
