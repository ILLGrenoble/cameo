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

#include "Services.h"

#include <unistd.h>
#include <iostream>
#include <sstream>
#include <stdexcept>
#include "impl/CancelIdGenerator.h"
#include "impl/ServicesImpl.h"
#include "impl/SocketImpl.h"
#include "ProtoType.h"

using namespace std;

namespace cameo {

Services::Services() :
	m_port(0),
	m_statusPort(0),
	m_impl(0) {
}

Services::~Services() {
	// Delete impl here to avoid order troubles.
	delete m_impl;
}

void Services::setImpl(ServicesImpl * impl) {
	m_impl = impl;
}

std::vector<std::string> Services::split(const std::string& info) {

	vector<string> result;

	int lastIndex = 0;
	int index = info.find(':');
	while (index != string::npos) {
		result.push_back(info.substr(lastIndex, index - lastIndex));
		lastIndex = index + 1;
		index = info.find(':', lastIndex);
	}
	result.push_back(info.substr(lastIndex));

	return result;
}

void Services::setTimeout(int timeout) {
	m_impl->setTimeout(timeout);
}

int Services::getTimeout() const {
	return m_impl->getTimeout();
}

const std::string& Services::getEndpoint() const {
	return m_serverEndpoint;
}

const std::string& Services::getUrl() const {
	return m_url;
}

int Services::getPort() const {
	return m_port;
}

const std::string& Services::getStatusEndpoint() const {
	return m_serverStatusEndpoint;
}

bool Services::isAvailable(int timeout) const {
	string strRequestType = m_impl->createRequest(PROTO_INIT);
	string strRequestData = m_impl->createInitRequest();
	return m_impl->isAvailable(strRequestType, strRequestData, m_serverEndpoint, timeout);
}

void Services::initStatus() {

	// get the status port
	string strRequestType = m_impl->createRequest(PROTO_STATUS);
	string strRequestData = m_impl->createShowStatusRequest();
	zmq::message_t* reply = m_impl->tryRequestWithOnePartReply(strRequestType, strRequestData, m_serverEndpoint);

	proto::RequestResponse requestResponse;
	requestResponse.ParseFromArray((*reply).data(), (*reply).size());
	delete reply;

	// reply ok
	if (requestResponse.value() == -1) {
		return;
	}

	m_statusPort = requestResponse.value();

	stringstream ss;
	ss << m_url << ":" << m_statusPort;
	m_serverStatusEndpoint = ss.str();
}

std::auto_ptr<EventStreamSocket> Services::openEventStream() {

	// init the status if needed
	if (m_statusPort == 0) {
		initStatus();
	}

	stringstream cancelEndpoint;

	// We define a unique name that depends on the event stream socket object because there can be many (instances).
	cancelEndpoint << "inproc://cancel." << CancelIdGenerator::newId();

	// create sockets
	zmq::socket_t * cancelPublisher = m_impl->createCancelPublisher(cancelEndpoint.str());
	zmq::socket_t * subscriber = m_impl->createEventSubscriber(m_serverStatusEndpoint, cancelEndpoint.str());

	// wait for the connection
	string strRequestType = m_impl->createRequest(PROTO_INIT);
	string strRequestData = m_impl->createInitRequest();
	m_impl->waitForSubscriber(subscriber, strRequestType, strRequestData, m_serverEndpoint);

	return auto_ptr<EventStreamSocket>(new EventStreamSocket(new SocketImpl(subscriber, cancelPublisher)));
}

}
