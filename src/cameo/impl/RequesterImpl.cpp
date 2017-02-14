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

#include <boost/bind.hpp>
#include <sstream>
#include "../Application.h"
#include "../Serializer.h"
#include "ApplicationImpl.h"

using namespace std;
using namespace boost;

namespace cameo {

const std::string RequesterImpl::REQUESTER_PREFIX = "req.";

RequesterImpl::RequesterImpl(const application::This * application, const std::string& url, int requesterPort, int responderPort, const std::string& name, int responderId) :
	m_application(application),
	m_requesterPort(requesterPort),
	m_name(name),
	m_responderId(responderId) {

	stringstream repEndpoint;
	repEndpoint << url << ":" << responderPort;
	m_responderEndpoint = repEndpoint.str();

	// create a socket REP
	m_requester.reset(new zmq::socket_t(m_application->m_impl->m_context, ZMQ_REP));
	stringstream reqEndpoint;
	reqEndpoint << "tcp://*:" << m_requesterPort;

	m_requester->bind(reqEndpoint.str().c_str());
}

RequesterImpl::~RequesterImpl() {
	terminate();
}

std::string RequesterImpl::getRequesterPortName(const std::string& name, int responderId) {

	stringstream requesterPortName;
	requesterPortName << REQUESTER_PREFIX << name << "." << responderId;

	return requesterPortName.str();
}

WaitingImpl * RequesterImpl::waiting() {
	return new GenericWaitingImpl(bind(&RequesterImpl::cancel, this));
}

void RequesterImpl::sendBinary(const std::string& request) {

	stringstream requesterEndpoint;
	requesterEndpoint << m_application->getUrl() << ":" << m_requesterPort;

	string strRequestType = m_application->m_impl->createRequest(PROTO_REQUEST);
	string strRequestData;

	proto::Request requestCommand;
	requestCommand.set_applicationid(m_application->getId());
	requestCommand.set_message(request);
	requestCommand.set_endpoint(requesterEndpoint.str());
	requestCommand.SerializeToString(&strRequestData);

	zmq::message_t* reply = m_application->m_impl->tryRequestWithOnePartReply(strRequestType, strRequestData, m_responderEndpoint);

	proto::RequestResponse requestResponse;
	requestResponse.ParseFromArray((*reply).data(), (*reply).size());
	delete reply;
}

void RequesterImpl::send(const std::string& request) {

	// encode the data
	string result;
	serialize(request, result);
	sendBinary(result);
}

void RequesterImpl::sendTwoBinaryParts(const std::string& request1, const std::string& request2) {

	stringstream requesterEndpoint;
	requesterEndpoint << m_application->getUrl() << ":" << m_requesterPort;

	string strRequestType = m_application->m_impl->createRequest(PROTO_REQUEST);
	string strRequestData;

	proto::Request requestCommand;
	requestCommand.set_applicationid(m_application->getId());
	requestCommand.set_message(request1);
	requestCommand.set_message2(request2);
	requestCommand.set_endpoint(requesterEndpoint.str());
	requestCommand.SerializeToString(&strRequestData);

	zmq::message_t* reply = m_application->m_impl->tryRequestWithOnePartReply(strRequestType, strRequestData, m_responderEndpoint);

	proto::RequestResponse requestResponse;
	requestResponse.ParseFromArray((*reply).data(), (*reply).size());
	delete reply;
}

bool RequesterImpl::receiveBinary(std::string& response) {

	zmq::message_t * message = new zmq::message_t;
	m_requester->recv(message, 0);

	// multi-part message, first part is the type
	proto::MessageType messageType;
	messageType.ParseFromArray((*message).data(), (*message).size());

	bool canceled = false;

	if (message->more()) {
		delete message;
		message = new zmq::message_t;
		m_requester->recv(message, 0);

	} else {
		cerr << "unexpected number of frames, should be 2" << endl;
		canceled = true;
	}

	if (messageType.type() == proto::MessageType_Type_RESPONSE) {
		response = string(static_cast<char*>(message->data()), message->size());

	} else if (messageType.type() == proto::MessageType_Type_CANCEL) {
		canceled = true;
	}

	// Create the reply
	string data = "OK";
	size_t size = data.length();
	zmq::message_t * reply = new zmq::message_t(size);
	memcpy((void *) reply->data(), data.c_str(), size);

	m_requester->send(*reply);

	delete reply;

	return !canceled;
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

	string strRequestType = m_application->m_impl->createRequest(PROTO_CANCEL);
	string strRequestData = "cancel";

	zmq::message_t* reply = m_application->m_impl->tryRequestWithOnePartReply(strRequestType, strRequestData, requesterEndpoint.str());

	proto::RequestResponse requestResponse;
	requestResponse.ParseFromArray((*reply).data(), (*reply).size());
	delete reply;
}

void RequesterImpl::terminate() {

	if (m_requester.get() != 0) {
		m_requester.reset(0);

		bool success = m_application->removePort(getRequesterPortName(m_name, m_responderId));
		if (!success) {
			cerr << "server cannot destroy requester " << m_name << endl;
		}
	}
}

}
