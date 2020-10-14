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
#include "impl/CancelIdGenerator.h"
#include "impl/ServicesImpl.h"
#include "impl/StreamSocketImpl.h"
#include "impl/RequestSocketImpl.h"
#include "message/Message.h"
#include <iostream>
#include <sstream>
#include <stdexcept>
#include "JSON.h"

using namespace std;

namespace cameo {

Services::Services() :
	m_port(0),
	m_statusPort(0),
	m_impl(nullptr) {

	m_serverVersion[0] = 0;
	m_serverVersion[1] = 0;
	m_serverVersion[2] = 0;
}

Services::~Services() {
	// Delete impl here to avoid order troubles.
	terminate();
}

void Services::terminate() {

	// Reset the request socket before the impl, otherwise reset impl will block.
	m_requestSocket.reset();

	// Reset the impl.
	m_impl.reset();
}

void Services::init() {
	// Set the impl.
	m_impl.reset(new ServicesImpl());
}

void Services::initRequestSocket() {
	// Create the request socket. The server endpoint must have been initialized.
	m_requestSocket = std::move(createRequestSocket(m_serverEndpoint, m_impl->m_timeout));
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

	if (m_requestSocket.get() != nullptr) {
		m_requestSocket->setTimeout(timeout);
	}
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

std::array<int, 3> Services::getVersion() const {
	return m_serverVersion;
}

int Services::getPort() const {
	return m_port;
}

const std::string& Services::getStatusEndpoint() const {
	return m_serverStatusEndpoint;
}

bool Services::isAvailable(int timeout) const {
	return m_impl->isAvailable(m_requestSocket.get(), timeout);
}

void Services::retrieveServerVersion() {

	// Get the version.
	unique_ptr<zmq::message_t> reply = m_requestSocket->request(m_impl->createVersionRequest());

	// Get the JSON response.
	json::Object response;
	json::parse(response, reply.get());

	m_serverVersion[0] = response[message::VersionResponse::MAJOR].GetInt();
	m_serverVersion[1] = response[message::VersionResponse::MINOR].GetInt();
	m_serverVersion[2] = response[message::VersionResponse::REVISION].GetInt();
}

void Services::initStatus() {

	// Get the status port.
	unique_ptr<zmq::message_t> reply = m_requestSocket->request(m_impl->createStreamStatusRequest());

	// Get the JSON response.
	json::Object response;
	json::parse(response, reply.get());

	int value = response[message::RequestResponse::VALUE].GetInt();

	// Check response.
	if (value == -1) {
		return;
	}

	// Get the status port.
	m_statusPort = value;

	stringstream ss;
	ss << m_url << ":" << m_statusPort;
	m_serverStatusEndpoint = ss.str();
}

std::unique_ptr<EventStreamSocket> Services::openEventStream() {

	// Init the status port if necessary.
	if (m_statusPort == 0) {
		initStatus();
	}

	stringstream cancelEndpoint;

	// We define a unique name that depends on the event stream socket object because there can be many (instances).
	cancelEndpoint << "inproc://cancel." << CancelIdGenerator::newId();

	// Create the sockets.
	zmq::socket_t * cancelPublisher = m_impl->createCancelPublisher(cancelEndpoint.str());
	zmq::socket_t * subscriber = m_impl->createEventSubscriber(m_serverStatusEndpoint, cancelEndpoint.str());

	// Wait for the connection to be ready.
	m_impl->waitForSubscriber(subscriber, m_requestSocket.get());

	// Create the event stream socket.
	return unique_ptr<EventStreamSocket>(new EventStreamSocket(new StreamSocketImpl(subscriber, cancelPublisher)));
}

int Services::getStreamPort(const std::string& name) {

	unique_ptr<zmq::message_t> reply = m_requestSocket->request(m_impl->createOutputPortRequest(name));

	// Get the JSON response.
	json::Object response;
	json::parse(response, reply.get());

	return response[message::RequestResponse::VALUE].GetInt();
}

std::unique_ptr<OutputStreamSocket> Services::createOutputStreamSocket(const std::string& name) {

	int port = getStreamPort(name);

	if (port == -1) {
		return nullptr;
	}

	// Prepare our context and subscriber.
	string streamEndpoint = m_url + ":" + to_string(port);

	// We define a unique name that depends on the event stream socket object because there can be many (instances).
	string cancelEndpoint = "inproc://cancel." + to_string(CancelIdGenerator::newId());

	// Create the sockets.
	zmq::socket_t * cancelPublisher = m_impl->createCancelPublisher(cancelEndpoint);
	zmq::socket_t * subscriber = m_impl->createOutputStreamSubscriber(streamEndpoint, cancelEndpoint);

	// Wait for the connection to be ready.
	m_impl->waitForStreamSubscriber(subscriber, m_requestSocket.get(), name);

	// Create the output stream socket.
	return unique_ptr<OutputStreamSocket>(new OutputStreamSocket(new StreamSocketImpl(subscriber, cancelPublisher)));
}

std::unique_ptr<RequestSocketImpl> Services::createRequestSocket(const std::string& endpoint) {
	return unique_ptr<RequestSocketImpl>(new RequestSocketImpl(m_impl.get(), endpoint, m_impl->m_timeout));
}

std::unique_ptr<RequestSocketImpl> Services::createRequestSocket(const std::string& endpoint, int timeout) {
	return unique_ptr<RequestSocketImpl>(new RequestSocketImpl(m_impl.get(), endpoint, timeout));
}

}
