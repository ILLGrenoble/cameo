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

#include <boost/bind.hpp>
#include <sstream>
#include "../Application.h"
#include "../Serializer.h"
#include "ApplicationImpl.h"
#include "RequestImpl.h"

using namespace std;
using namespace boost;

namespace cameo {

const std::string ResponderImpl::RESPONDER_PREFIX = "rep.";

ResponderImpl::ResponderImpl(const application::This * application, int responderPort, const std::string& name) :
	m_application(application),
	m_responderPort(responderPort),
	m_name(name),
	m_ended(false) {

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

	stringstream endpoint;
	endpoint << m_application->getUrl() << ":" << m_responderPort;

	string strRequestType = m_application->m_impl->createRequest(PROTO_CANCEL);
	string strRequestData = "cancel";

	unique_ptr<zmq::message_t> reply = m_application->m_impl->tryRequestWithOnePartReply(strRequestType, strRequestData, endpoint.str());

	proto::RequestResponse requestResponse;
	requestResponse.ParseFromArray((*reply).data(), (*reply).size());
}

WaitingImpl * ResponderImpl::waiting() {
	return new GenericWaitingImpl(bind(&ResponderImpl::cancel, this));
}

std::unique_ptr<RequestImpl> ResponderImpl::receive() {

	zmq::message_t * message = new zmq::message_t;
	m_responder->recv(message, 0);

	// multi-part message, first part is the type
	proto::MessageType messageType;
	messageType.ParseFromArray((*message).data(), (*message).size());

	if (message->more()) {
		delete message;
		message = new zmq::message_t;
		m_responder->recv(message, 0);

	} else {
		cerr << "unexpected number of frames, should be 2" << endl;
		m_ended = true;

		return unique_ptr<RequestImpl>(nullptr);
	}

	// Create the reply
	string data = "OK";
	size_t size = data.length();
	zmq::message_t * reply = new zmq::message_t(size);
	memcpy((void *) reply->data(), data.c_str(), size);

	unique_ptr<RequestImpl> result;

	if (messageType.type() == proto::MessageType_Type_REQUEST) {

		// Parse the message
		proto::Request messageRequest;
		messageRequest.ParseFromArray((*message).data(), (*message).size());

		// Create the request
		result = unique_ptr<RequestImpl>(new RequestImpl(m_application,
				messageRequest.applicationname(),
				messageRequest.applicationid(),
				messageRequest.message(),
				messageRequest.serverurl(),
				messageRequest.serverport(),
				messageRequest.requesterport()));

		// Set message 2 if it exists.
		if (messageRequest.has_message2()) {
			result->m_message2 = messageRequest.message2();
		}

	} else if (messageType.type() == proto::MessageType_Type_CANCEL) {
		m_ended = true;

	} else {
		cerr << "unknown message type " << messageType.type() << endl;
		m_responder->send(*message);
	}

	// send to the client
	if (reply != 0) {
		m_responder->send(*reply);
	}

	delete reply;
	delete message;

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
