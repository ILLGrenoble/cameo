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

#include "Application.h"
#include "Serializer.h"
#include "JSON.h"
#include "../../base/impl/RequestSocketImpl.h"
#include "../../base/impl/ContextImpl.h"
#include "../../base/Messages.h"
#include <sstream>

namespace cameo {
namespace coms {

const std::string RequesterImpl::REQUESTER_PREFIX = "req.";
std::mutex RequesterImpl::m_mutex;
int RequesterImpl::m_requesterCounter = 0;

RequesterImpl::RequesterImpl(const Endpoint& endpoint, int requesterPort, int responderPort, const std::string& name, int responderId, int requesterId) :
	m_requesterPort(requesterPort),
	m_name(name),
	m_responderId(responderId),
	m_requesterId(requesterId),
	m_canceled(false) {

	// Create the request socket.
	m_requestSocket = application::This::getCom().createRequestSocket(endpoint.withPort(responderPort).toString());

	// Create a socket REP.
	ContextImpl* contextImpl = dynamic_cast<ContextImpl *>(application::This::getCom().getContext());
	m_repSocket.reset(new zmq::socket_t(contextImpl->m_context, ZMQ_REP));
	std::stringstream reqEndpoint;
	reqEndpoint << "tcp://*:" << m_requesterPort;

	m_repSocket->bind(reqEndpoint.str().c_str());
}

RequesterImpl::~RequesterImpl() {
	terminate();
}

int RequesterImpl::newRequesterId() {

	std::lock_guard<std::mutex> lock(m_mutex);
	m_requesterCounter++;

	return m_requesterCounter;
}

std::string RequesterImpl::getRequesterPortName(const std::string& name, int responderId, int requesterId) {

	std::stringstream requesterPortName;
	requesterPortName << REQUESTER_PREFIX << name << "." << responderId << "." << requesterId;

	return requesterPortName.str();
}

WaitingImpl * RequesterImpl::waiting() {
	return new GenericWaitingImpl(std::bind(&RequesterImpl::cancel, this));
}

void RequesterImpl::sendBinary(const std::string& requestData) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushInt(message::REQUEST);

	request.pushKey(message::Request::APPLICATION_NAME);
	request.pushString(application::This::getName());

	request.pushKey(message::Request::APPLICATION_ID);
	request.pushInt(application::This::getId());

	request.pushKey(message::Request::SERVER_URL);
	request.pushString(application::This::getEndpoint().getProtocol() + "://" + application::This::getEndpoint().getAddress());

	request.pushKey(message::Request::SERVER_PORT);
	request.pushInt(application::This::getEndpoint().getPort());

	request.pushKey(message::Request::REQUESTER_PORT);
	request.pushInt(m_requesterPort);

	m_requestSocket->request(request.toString(), requestData);
}

void RequesterImpl::send(const std::string& requestData) {

	// encode the data
	std::string result;
	serialize(requestData, result);
	sendBinary(result);
}

void RequesterImpl::sendTwoBinaryParts(const std::string& requestData1, const std::string& requestData2) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushInt(message::REQUEST);

	request.pushKey(message::Request::APPLICATION_NAME);
	request.pushString(application::This::getName());

	request.pushKey(message::Request::APPLICATION_ID);
	request.pushInt(application::This::getId());

	request.pushKey(message::Request::SERVER_URL);
	request.pushString(application::This::getEndpoint().getProtocol() + "://" + application::This::getEndpoint().getAddress());

	request.pushKey(message::Request::SERVER_PORT);
	request.pushInt(application::This::getEndpoint().getPort());

	request.pushKey(message::Request::REQUESTER_PORT);
	request.pushInt(m_requesterPort);

	m_requestSocket->request(request.toString(), requestData1, requestData2);
}

std::optional<std::string> RequesterImpl::receiveBinary() {

	if (m_canceled) {
		return {};
	}

	std::unique_ptr<zmq::message_t> message(new zmq::message_t);
	m_repSocket->recv(message.get(), 0);

	// Get the JSON request.
	json::Object request;
	json::parse(request, message.get());

	int type = request[message::TYPE].GetInt();

	if (type == message::CANCEL) {
		m_canceled = true;
		return {};
	}

	std::optional<std::string> result;

	if (type == message::RESPONSE) {
		// Get the second part for the message.
		message.reset(new zmq::message_t);
		m_repSocket->recv(message.get(), 0);
		result = std::string(message->data<char>(), message->size());
	}

	// Create the reply.
	std::string data = "OK";
	size_t size = data.length();
	std::unique_ptr<zmq::message_t> reply(new zmq::message_t(size));
	memcpy(reply->data(), data.c_str(), size);

	m_repSocket->send(*reply);

	return result;
}

std::optional<std::string> RequesterImpl::receive() {
	return receiveBinary();
}

void RequesterImpl::cancel() {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushInt(message::CANCEL);

	// Create a request socket only for the request.
	std::unique_ptr<RequestSocketImpl> requestSocket = application::This::getCom().createRequestSocket(application::This::getEndpoint().withPort(m_requesterPort).toString());
	requestSocket->request(request.toString());
}

void RequesterImpl::terminate() {

	if (m_repSocket.get() != nullptr) {
		m_repSocket.reset(nullptr);

		application::This::getCom().removePort(getRequesterPortName(m_name, m_responderId, m_requesterId));
	}

	m_requestSocket.reset();
}

}
}

