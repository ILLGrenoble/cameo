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

#include "SocketException.h"
#include "ConnectionTimeout.h"
#include "JSON.h"
#include "../Messages.h"

#include <iostream>
#include <sstream>
#include "ContextImpl.h"

#include "../RequestSocket.h"

using namespace std;

namespace cameo {

ContextImpl::ContextImpl() : Context(),
	m_context(1), m_timeout(0) {
}

ContextImpl::~ContextImpl() {
}

void ContextImpl::setTimeout(int timeout) {
	m_timeout = timeout;
}

int ContextImpl::getTimeout() const {
	return m_timeout;
}

zmq::socket_t * ContextImpl::createEventSubscriber(const std::string& endpoint, const std::string& cancelEndpoint) {

	zmq::socket_t * subscriber = new zmq::socket_t(m_context, ZMQ_SUB);

	vector<string> streamList;
	streamList.push_back(message::Event::STATUS);
	streamList.push_back(message::Event::RESULT);
	streamList.push_back(message::Event::PUBLISHER);
	streamList.push_back(message::Event::PORT);
	streamList.push_back(message::Event::KEYVALUE);
	streamList.push_back(message::Event::CANCEL);

	for (vector<string>::const_iterator s = streamList.begin(); s != streamList.end(); ++s) {
		subscriber->setsockopt(ZMQ_SUBSCRIBE, s->c_str(), s->length());
	}

	subscriber->connect(endpoint.c_str());
	subscriber->connect(cancelEndpoint.c_str());

	return subscriber;
}

zmq::socket_t * ContextImpl::createOutputStreamSubscriber(const std::string& endpoint, const std::string& cancelEndpoint) {

	zmq::socket_t * subscriber = new zmq::socket_t(m_context, ZMQ_SUB);

	vector<string> streamList;
	streamList.push_back(message::Event::SYNCSTREAM);
	streamList.push_back(message::Event::STREAM);
	streamList.push_back(message::Event::ENDSTREAM);
	streamList.push_back(message::Event::CANCEL);

	for (vector<string>::const_iterator s = streamList.begin(); s != streamList.end(); ++s) {
		subscriber->setsockopt(ZMQ_SUBSCRIBE, s->c_str(), s->length());
	}

	subscriber->connect(endpoint.c_str());
	subscriber->connect(cancelEndpoint.c_str());

	return subscriber;
}

zmq::socket_t * ContextImpl::createCancelPublisher(const std::string& endpoint) {

	zmq::socket_t * publisher = new zmq::socket_t(m_context, ZMQ_PUB);
	publisher->bind(endpoint.c_str());

	return publisher;
}

zmq::socket_t * ContextImpl::createRequestSocket(const std::string& endpoint) {

	zmq::socket_t* socket = new zmq::socket_t(m_context, ZMQ_REQ);

	try {
		// Set the linger value to 0 to ensure that pending requests are destroyed in case of timeout.
		int value = 0;
		socket->setsockopt(ZMQ_LINGER, &value, sizeof(int));

		// Connect to the endpoint.
		socket->connect(endpoint.c_str());
	}
	catch (exception const & e) {
		throw SocketException(e.what());
	}

	return socket;
}

bool ContextImpl::isAvailable(RequestSocket * socket, int timeout) {

	try {
		socket->requestJSON(createSyncRequest(), timeout);
		return true;
	}
	catch (const ConnectionTimeout&) {
		// The server is not accessible.
	}
	catch (...) {
		// Should not happen.
	}

	return false;
}

void ContextImpl::sendSyncStream(RequestSocket * socket, const std::string& name) {

	try {
		socket->requestJSON(createSyncStreamRequest(name));
	}
	catch (const ConnectionTimeout&) {
		// The server is not accessible.
	}
	catch (...) {
		// Should not happen.
	}
}

void ContextImpl::waitForStreamSubscriber(zmq::socket_t * subscriber, RequestSocket * socket, const std::string& name) {

	// Poll subscriber.
	zmq_pollitem_t items[1];
	items[0].socket = static_cast<void *>(*subscriber);
	items[0].fd = 0;
	items[0].events = ZMQ_POLLIN;
	items[0].revents = 0;

	while (true) {
		sendSyncStream(socket, name);

		// Wait for 100ms.
		int rc = zmq::poll(items, 1, 100);
		if (rc != 0) {
			break;
		}
	}
}

void ContextImpl::waitForSubscriber(zmq::socket_t * subscriber, RequestSocket * socket) {

	// Poll subscriber.
	zmq_pollitem_t items[1];
	items[0].socket = static_cast<void *>(*subscriber);
	items[0].fd = 0;
	items[0].events = ZMQ_POLLIN;
	items[0].revents = 0;

	while (true) {
		isAvailable(socket, 100);

		// Wait for 100ms.
		int rc = zmq::poll(items, 1, 100);
		if (rc != 0) {
			break;
		}
	}
}

}
