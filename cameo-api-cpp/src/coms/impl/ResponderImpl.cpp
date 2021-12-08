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

#include "ResponderImpl.h"

#include "RequestImpl.h"
#include "Application.h"
#include "Serializer.h"
#include "JSON.h"
#include "../../base/impl/RequestSocketImpl.h"
#include "../../base/impl/ContextImpl.h"
#include "../../base/Messages.h"
#include <sstream>

namespace cameo {
namespace coms {

const std::string ResponderImpl::RESPONDER_PREFIX = "rep.";

ResponderImpl::ResponderImpl(int responderPort, const std::string& name) :
	m_responderPort(responderPort),
	m_name(name),
	m_canceled(false) {

	// create a socket REP
	ContextImpl* contextImpl = dynamic_cast<ContextImpl *>(application::This::getCom().getContext());
	m_responder.reset(new zmq::socket_t(contextImpl->m_context, ZMQ_REP));
	std::stringstream repEndpoint;
	repEndpoint << "tcp://*:" << m_responderPort;

	m_responder->bind(repEndpoint.str().c_str());
}

ResponderImpl::~ResponderImpl() {
	terminate();
}

void ResponderImpl::cancel() {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushInt(message::CANCEL);

	// Create a request socket.
	std::unique_ptr<RequestSocketImpl> requestSocket = application::This::getCom().createRequestSocket(application::This::getEndpoint().withPort(m_responderPort).toString());
	requestSocket->requestJSON(request.toString());
}

WaitingImpl * ResponderImpl::waiting() {
	return new GenericWaitingImpl(std::bind(&ResponderImpl::cancel, this));
}

std::unique_ptr<RequestImpl> ResponderImpl::receive() {

	std::unique_ptr<zmq::message_t> message(new zmq::message_t);
	m_responder->recv(message.get(), 0);

	// Get the JSON request.
	json::Object request;
	json::parse(request, message.get());

	int type = request[message::TYPE].GetInt();

	// Create the reply
	std::unique_ptr<zmq::message_t> reply;
	std::unique_ptr<RequestImpl> result;

	if (type == message::REQUEST) {

		std::string name = request[message::Request::APPLICATION_NAME].GetString();
		int id = request[message::Request::APPLICATION_ID].GetInt();
		std::string serverUrl = request[message::Request::SERVER_URL].GetString();
		int serverPort = request[message::Request::SERVER_PORT].GetInt();
		int requesterPort = request[message::Request::REQUESTER_PORT].GetInt();

		// Get the second part for the message.
		message.reset(new zmq::message_t);
		m_responder->recv(message.get(), 0);
		std::string message1(message->data<char>(), message->size());

		// Create the request.
		result = std::unique_ptr<RequestImpl>(new RequestImpl(name,
				id,
				message1,
				serverUrl,
				serverPort,
				requesterPort));

		// Set message 2 if it exists.
		if (message->more()) {
			message.reset(new zmq::message_t);
			m_responder->recv(message.get(), 0);
			result->m_message2 = std::string(message->data<char>(), message->size());
		}
		reply.reset(processRequest());
	}
	else if (type == message::CANCEL) {
		m_canceled = true;
		reply.reset(processCancelResponder());
	}
	else {
		reply.reset(processUnknownRequest());
	}

	// send to the client
	if (reply != nullptr) {
		m_responder->send(*reply);
	}

	return result;
}

zmq::message_t * ResponderImpl::processRequest() {

	std::string result = createRequestResponse(0, "OK");

	zmq::message_t * reply = new zmq::message_t(result.length());
	memcpy(reply->data(), result.c_str(), result.length());

	return reply;
}

zmq::message_t * ResponderImpl::processCancelResponder() {

	std::string result = createRequestResponse(0, "OK");

	zmq::message_t * reply = new zmq::message_t(result.length());
	memcpy(reply->data(), result.c_str(), result.length());

	return reply;
}

zmq::message_t * ResponderImpl::processUnknownRequest() {

	std::string result = createRequestResponse(-1, "Unknown request");

	zmq::message_t * reply = new zmq::message_t(result.length());
	memcpy(reply->data(), result.c_str(), result.length());

	return reply;
}

void ResponderImpl::terminate() {

	if (m_responder.get() != nullptr) {
		m_responder.reset(nullptr);

		application::This::getCom().removePort(RESPONDER_PREFIX + m_name);
	}
}

}
}

