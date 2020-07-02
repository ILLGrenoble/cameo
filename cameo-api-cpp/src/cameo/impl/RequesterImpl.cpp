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

#include "RequesterImpl.h"
#include "../Application.h"
#include "../Serializer.h"
#include "ServicesImpl.h"
#include "RequestSocketImpl.h"
#include <sstream>

using namespace std;

namespace cameo {

const std::string RequesterImpl::REQUESTER_PREFIX = "req.";
std::mutex RequesterImpl::m_mutex;
int RequesterImpl::m_requesterCounter = 0;

RequesterImpl::RequesterImpl(application::This * application, const std::string& url, int requesterPort, int responderPort, const std::string& name, int responderId, int requesterId) :
	m_application(application),
	m_requesterPort(requesterPort),
	m_name(name),
	m_responderId(responderId),
	m_requesterId(requesterId),
	m_canceled(false) {

	stringstream repEndpoint;
	repEndpoint << url << ":" << responderPort;
	m_responderEndpoint = repEndpoint.str();

	// Create the request socket.
	m_requestSocket = m_application->createRequestSocket(m_responderEndpoint);

	// Create a socket REP.
	m_repSocket.reset(new zmq::socket_t(m_application->m_impl->m_context, ZMQ_REP));
	stringstream reqEndpoint;
	reqEndpoint << "tcp://*:" << m_requesterPort;

	m_repSocket->bind(reqEndpoint.str().c_str());
}

RequesterImpl::~RequesterImpl() {
	terminate();
}

int RequesterImpl::newRequesterId() {

	lock_guard<mutex> lock(m_mutex);
	m_requesterCounter++;

	return m_requesterCounter;
}

std::string RequesterImpl::getRequesterPortName(const std::string& name, int responderId, int requesterId) {

	stringstream requesterPortName;
	requesterPortName << REQUESTER_PREFIX << name << "." << responderId << "." << requesterId;

	return requesterPortName.str();
}

WaitingImpl * RequesterImpl::waiting() {
	return new GenericWaitingImpl(bind(&RequesterImpl::cancel, this));
}

void RequesterImpl::sendBinary(const std::string& request) {

	string requestTypePart = m_application->m_impl->createRequestType(PROTO_REQUEST);
	string requestDataPart;

	proto::Request requestCommand;
	requestCommand.set_applicationname(m_application->getName());
	requestCommand.set_applicationid(m_application->getId());
	requestCommand.set_message(request);
	requestCommand.set_serverurl(m_application->getUrl());
	requestCommand.set_serverport(m_application->getPort());
	requestCommand.set_requesterport(m_requesterPort);
	requestCommand.SerializeToString(&requestDataPart);

	unique_ptr<zmq::message_t> reply = m_requestSocket->request(requestTypePart, requestDataPart);

	proto::RequestResponse requestResponse;
	requestResponse.ParseFromArray((*reply).data(), (*reply).size());
}

void RequesterImpl::send(const std::string& request) {

	// encode the data
	string result;
	serialize(request, result);
	sendBinary(result);
}

void RequesterImpl::sendTwoBinaryParts(const std::string& request1, const std::string& request2) {

	string requestTypePart = m_application->m_impl->createRequestType(PROTO_REQUEST);
	string requestDataPart;

	proto::Request requestCommand;
	requestCommand.set_applicationname(m_application->getName());
	requestCommand.set_applicationid(m_application->getId());
	requestCommand.set_message(request1);
	requestCommand.set_message2(request2);
	requestCommand.set_serverurl(m_application->getUrl());
	requestCommand.set_serverport(m_application->getPort());
	requestCommand.set_requesterport(m_requesterPort);
	requestCommand.SerializeToString(&requestDataPart);

	unique_ptr<zmq::message_t> reply = m_requestSocket->request(requestTypePart, requestDataPart);

	proto::RequestResponse requestResponse;
	requestResponse.ParseFromArray((*reply).data(), (*reply).size());
}

bool RequesterImpl::receiveBinary(std::string& response) {

	unique_ptr<zmq::message_t> message(new zmq::message_t);
	m_repSocket->recv(message.get(), 0);

	// multi-part message, first part is the type
	proto::MessageType messageType;
	messageType.ParseFromArray((*message).data(), (*message).size());

	if (message->more()) {
		message.reset(new zmq::message_t);
		m_repSocket->recv(message.get(), 0);

	} else {
		cerr << "unexpected number of frames, should be 2" << endl;
		m_canceled = true;
	}

	if (messageType.type() == proto::MessageType_Type_RESPONSE) {
		response = string(static_cast<char*>(message->data()), message->size());

	} else if (messageType.type() == proto::MessageType_Type_CANCEL) {
		m_canceled = true;
	}

	// Create the reply
	string data = "OK";
	size_t size = data.length();
	unique_ptr<zmq::message_t> reply(new zmq::message_t(size));
	memcpy((void *) reply->data(), data.c_str(), size);

	m_repSocket->send(*reply);

	return !m_canceled;
}

bool RequesterImpl::receive(std::string& data) {

	string bytes;
	bool result = receiveBinary(bytes);

	parse(bytes, data);

	return result;
}

void RequesterImpl::cancel() {

	stringstream requesterEndpoint;
	requesterEndpoint << m_application->getUrl() << ":" << m_requesterPort;

	// Create a request socket only for the request.
	unique_ptr<RequestSocketImpl> requestSocket = m_application->createRequestSocket(requesterEndpoint.str());
	unique_ptr<zmq::message_t> reply = requestSocket->request(m_application->m_impl->createRequestType(PROTO_CANCEL), "cancel");

	proto::RequestResponse requestResponse;
	requestResponse.ParseFromArray((*reply).data(), (*reply).size());
}

void RequesterImpl::terminate() {

	if (m_repSocket.get() != nullptr) {
		m_repSocket.reset(nullptr);

		bool success = m_application->removePort(getRequesterPortName(m_name, m_responderId, m_requesterId));
		if (!success) {
			cerr << "server cannot destroy requester " << m_name << endl;
		}
	}

	m_requestSocket.reset();
}

}
