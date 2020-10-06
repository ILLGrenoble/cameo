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
#include "../message/JSON.h"
#include "../message/Message.h"
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

void RequesterImpl::sendBinary(const std::string& requestData) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushInt(message::REQUEST);

	request.pushKey(message::Request::APPLICATION_NAME);
	request.pushString(m_application->getName());

	request.pushKey(message::Request::APPLICATION_ID);
	request.pushInt(m_application->getId());

	request.pushKey(message::Request::SERVER_URL);
	request.pushString(m_application->getUrl());

	request.pushKey(message::Request::SERVER_PORT);
	request.pushInt(m_application->getPort());

	request.pushKey(message::Request::REQUESTER_PORT);
	request.pushInt(m_requesterPort);

	m_requestSocket->request(request.toString(), requestData);
}

void RequesterImpl::send(const std::string& requestData) {

	// encode the data
	string result;
	serialize(requestData, result);
	sendBinary(result);
}

void RequesterImpl::sendTwoBinaryParts(const std::string& requestData1, const std::string& requestData2) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushInt(message::REQUEST);

	request.pushKey(message::Request::APPLICATION_NAME);
	request.pushString(m_application->getName());

	request.pushKey(message::Request::APPLICATION_ID);
	request.pushInt(m_application->getId());

	request.pushKey(message::Request::SERVER_URL);
	request.pushString(m_application->getUrl());

	request.pushKey(message::Request::SERVER_PORT);
	request.pushInt(m_application->getPort());

	request.pushKey(message::Request::REQUESTER_PORT);
	request.pushInt(m_requesterPort);

	m_requestSocket->request(request.toString(), requestData1, requestData2);
}

bool RequesterImpl::receiveBinary(std::string& response) {

	unique_ptr<zmq::message_t> message(new zmq::message_t);
	m_repSocket->recv(message.get(), 0);

	// Get the JSON request.
	json::Object request;
	json::parse(request, message.get());

	int type = request[message::TYPE].GetInt();

	if (type == message::RESPONSE) {
		// Get the second part for the message.
		message.reset(new zmq::message_t);
		m_repSocket->recv(message.get(), 0);
		response = string(message->data<char>(), message->size());
	}
	else if (type == message::CANCEL) {
		m_canceled = true;
	}

	// Create the reply.
	string data = "OK";
	size_t size = data.length();
	unique_ptr<zmq::message_t> reply(new zmq::message_t(size));
	memcpy(reply->data(), data.c_str(), size);

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

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushInt(message::CANCEL);

	// Create a request socket only for the request.
	unique_ptr<RequestSocketImpl> requestSocket = m_application->createRequestSocket(requesterEndpoint.str());
	requestSocket->request(request.toString());
}

void RequesterImpl::terminate() {

	if (m_repSocket.get() != nullptr) {
		m_repSocket.reset(nullptr);

		bool success = m_application->removePort(getRequesterPortName(m_name, m_responderId, m_requesterId));
		if (!success) {
			cerr << "Server cannot destroy requester " << m_name << endl;
		}
	}

	m_requestSocket.reset();
}

}
