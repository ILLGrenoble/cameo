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
#include "../../base/impl/ServicesImpl.h"
#include "../../base/Requests.h"
#include <sstream>

using namespace std;

namespace cameo {
namespace coms {

const std::string ResponderImpl::RESPONDER_PREFIX = "rep.";

ResponderImpl::ResponderImpl(application::This * application, int responderPort, const std::string& name) :
	m_application(application),
	m_responderPort(responderPort),
	m_name(name),
	m_canceled(false) {

	// create a socket REP
	m_responder.reset(new zmq::socket_t(m_application->m_impl->m_context, ZMQ_REP));
	stringstream repEndpoint;
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
	unique_ptr<RequestSocketImpl> requestSocket = m_application->createRequestSocket(m_application->getEndpoint().withPort(m_responderPort).toString());
	requestSocket->request(request.toString());
}

WaitingImpl * ResponderImpl::waiting() {
	return new GenericWaitingImpl(bind(&ResponderImpl::cancel, this));
}

std::unique_ptr<RequestImpl> ResponderImpl::receive() {

	unique_ptr<zmq::message_t> message(new zmq::message_t);
	m_responder->recv(message.get(), 0);

	// Get the JSON request.
	json::Object request;
	json::parse(request, message.get());

	int type = request[message::TYPE].GetInt();

	// Create the reply
	string data = "OK";
	size_t size = data.length();
	unique_ptr<zmq::message_t> reply(new zmq::message_t(size));
	memcpy(reply->data(), data.c_str(), size);

	unique_ptr<RequestImpl> result;

	if (type == message::REQUEST) {

		string name = request[message::Request::APPLICATION_NAME].GetString();
		int id = request[message::Request::APPLICATION_ID].GetInt();
		string serverUrl = request[message::Request::SERVER_URL].GetString();
		int serverPort = request[message::Request::SERVER_PORT].GetInt();
		int requesterPort = request[message::Request::REQUESTER_PORT].GetInt();

		// Get the second part for the message.
		message.reset(new zmq::message_t);
		m_responder->recv(message.get(), 0);
		string message1(message->data<char>(), message->size());

		// Create the request.
		result = unique_ptr<RequestImpl>(new RequestImpl(m_application,
				name,
				id,
				message1,
				serverUrl,
				serverPort,
				requesterPort));

		// Set message 2 if it exists.
		if (message->more()) {
			message.reset(new zmq::message_t);
			m_responder->recv(message.get(), 0);
			result->m_message2 = string(message->data<char>(), message->size());
		}
	}
	else if (type == message::CANCEL) {
		m_canceled = true;
	}
	else {
		cerr << "Unknown message type " << type << endl;
		m_responder->send(*message);
	}

	// send to the client
	if (reply != nullptr) {
		m_responder->send(*reply);
	}

	return result;
}

void ResponderImpl::terminate() {

	if (m_responder.get() != nullptr) {
		m_responder.reset(nullptr);

		bool success = m_application->removePort(RESPONDER_PREFIX + m_name);
		if (!success) {
			cerr << "server cannot destroy responder " << m_name << endl;
		}
	}
}

}
}

