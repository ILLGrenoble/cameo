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

#include "Server.h"

#include <iostream>
#include <sstream>
#include "Application.h"
#include "ConnectionChecker.h"
#include "impl/ServicesImpl.h"
#include "ProtoType.h"
#include "EventThread.h"
#include "impl/StreamSocketImpl.h"

using namespace std;

namespace cameo {

Server::Server(const std::string& endpoint) :
	Services() {

	Services::init();

	vector<string> tokens = split(endpoint);

	if (tokens.size() < 3) {
		throw InvalidArgumentException(endpoint + " is not a valid endpoint");
	}

	m_url = tokens[0] + ":" + tokens[1];
	string port = tokens[2];
	istringstream is(port);
	is >> m_port;
	m_serverEndpoint = m_url + ":" + port;

	// Create the request socket. The server endpoint has been defined.
	Services::initRequestSocket();

	// Start the event thread.
	unique_ptr<EventStreamSocket> socket = openEventStream();
	m_eventThread.reset(new EventThread(this, socket));
	m_eventThread->start();
}

Server::~Server() {
	// Stop the event thread.
	if (m_eventThread.get() != nullptr) {
		m_eventThread->cancel();
	}
}

void Server::setTimeout(int timeoutMs) {
	Services::setTimeout(timeoutMs);
}

int Server::getTimeout() const {
	return Services::getTimeout();
}

const std::string& Server::getEndpoint() const {
	return Services::getEndpoint();
}

const std::string& Server::getUrl() const {
	return Services::getUrl();
}

int Server::getPort() const {
	return Services::getPort();
}

bool Server::isAvailable(int timeout) const {
	return Services::isAvailable(timeout);
}

bool Server::isAvailable() const {
	return isAvailable(getAvailableTimeout());
}

int Server::getAvailableTimeout() const {
	int timeout = getTimeout();
	if (timeout > 0) {
		return timeout;
	}
	else {
		return 10000;
	}
}

std::unique_ptr<application::Instance> Server::makeInstance() {
	return unique_ptr<application::Instance>(new application::Instance(this));
}

std::unique_ptr<application::Instance> Server::start(const std::string& name, Option options) {
	return start(name, vector<string>(), options);
}

int Server::getStreamPort(const std::string& name) {

	string strRequestType = m_impl->createRequestType(PROTO_OUTPUT);
	string strRequestData = m_impl->createOutputRequest(name);

	unique_ptr<zmq::message_t> reply = m_impl->tryRequestWithOnePartReply(strRequestType, strRequestData, m_serverEndpoint);

	proto::RequestResponse requestResponse;
	requestResponse.ParseFromArray((*reply).data(), (*reply).size());

	return requestResponse.value();
}

std::unique_ptr<application::Instance> Server::start(const std::string& name, const std::vector<std::string> & args, Option options) {

	bool outputStream = ((options & OUTPUTSTREAM) != 0);

	unique_ptr<application::Instance> instance = makeInstance();

	// Set the name and register the instance as event listener.
	instance->setName(name);
	registerEventListener(instance.get());

	try {
		if (outputStream) {
			// we connect to the stream port before starting the application
			// so that we are sure that the ENDSTREAM message will be received even if the application terminates rapidly
			unique_ptr<OutputStreamSocket> socket = createOutputStreamSocket(getStreamPort(name));
			instance->setOutputStreamSocket(socket);
		}

		string strRequestType = m_impl->createRequestType(PROTO_START);
		string strRequestData = m_impl->createStartRequest(name, args, application::This::getReference());

		unique_ptr<zmq::message_t> reply = m_impl->tryRequestWithOnePartReply(strRequestType, strRequestData, m_serverEndpoint);

		proto::RequestResponse requestResponse;
		requestResponse.ParseFromArray((*reply).data(), (*reply).size());

		if (requestResponse.value() == -1) {
			instance->setErrorMessage(requestResponse.message());
		} else {
			instance->setId(requestResponse.value());
		}

	} catch (const ConnectionTimeout& e) {
		instance->setErrorMessage(e.what());
	}

	return instance;
}

Response Server::stopApplicationAsynchronously(int id, bool immediately) const {

	string strRequestType;
	string strRequestData;

	if (immediately) {
		strRequestType = m_impl->createRequestType(PROTO_KILL);
		strRequestData = m_impl->createKillRequest(id);
	} else {
		strRequestType = m_impl->createRequestType(PROTO_STOP);
		strRequestData = m_impl->createStopRequest(id);
	}

	unique_ptr<zmq::message_t> reply = m_impl->tryRequestWithOnePartReply(strRequestType, strRequestData, m_serverEndpoint);

	proto::RequestResponse requestResponse;
	requestResponse.ParseFromArray((*reply).data(), (*reply).size());

	return Response(requestResponse.value(), requestResponse.message());
}

application::InstanceArray Server::connectAll(const std::string& name, Option options) {

	bool outputStream = ((options & OUTPUTSTREAM) != 0);

	application::InstanceArray instances;

	string strRequestType = m_impl->createRequestType(PROTO_CONNECT);
	string strRequestData = m_impl->createConnectRequest(name);
	unique_ptr<zmq::message_t> reply = m_impl->tryRequestWithOnePartReply(strRequestType, strRequestData, m_serverEndpoint);

	proto::ApplicationInfoListResponse response;
	response.ParseFromArray((*reply).data(), (*reply).size());

	// allocate the array
	instances.allocate(response.applicationinfo_size());

	int aliveInstancesCount = 0;

	for (int i = 0; i < response.applicationinfo_size(); ++i) {
		proto::ApplicationInfo info = response.applicationinfo(i);

		unique_ptr<application::Instance> instance = makeInstance();

		// Set the name and register the instance as event listener.
		instance->setName(info.name());
		registerEventListener(instance.get());

		int applicationId = info.id();

		// test if the application is still alive otherwise we could have missed a status message
		if (isAlive(applicationId)) {
			aliveInstancesCount++;

			// we connect to the stream port before starting the application
			// so that we are sure that the ENDSTREAM message will be received even if the application terminates rapidly
			if (outputStream) {
				unique_ptr<OutputStreamSocket> socket = createOutputStreamSocket(getStreamPort(info.name()));
				instance->setOutputStreamSocket(socket);
			}

			instance->setId(applicationId);
			instance->setInitialState(info.applicationstate());
			instance->setPastStates(info.pastapplicationstates());

			instances.m_array[i] = std::move(instance);
		}
	}

	// Copy the instances alive
	application::InstanceArray aliveInstances;
	aliveInstances.allocate(aliveInstancesCount);

	int j = 0;
	for (int i = 0; i < response.applicationinfo_size(); ++i) {

		if (instances.m_array[i].get() != 0) {
			aliveInstances[j] = std::move(instances.m_array[i]);
			j++;
		}
	}

	return aliveInstances;
}

std::unique_ptr<application::Instance> Server::connect(const std::string& name, Option options) {

	application::InstanceArray instances = connectAll(name, options);

	if (instances.size() == 0) {
		unique_ptr<application::Instance> instance = makeInstance();
		return instance;
	}

	return std::move(instances[0]);
}

void Server::killAllAndWaitFor(const std::string& name) {

	application::InstanceArray instances = connectAll(name);

	for (int i = 0; i < instances.size(); ++i) {
		instances[i]->kill();
		instances[i]->waitFor();
	}
}

bool Server::isAlive(int id) const {

	string strRequestType = m_impl->createRequestType(PROTO_ISALIVE);
	string strRequestData = m_impl->createIsAliveRequest(id);
	unique_ptr<zmq::message_t> reply = m_impl->tryRequestWithOnePartReply(strRequestType, strRequestData, m_serverEndpoint);

	proto::IsAliveResponse isAliveResponse;
	isAliveResponse.ParseFromArray((*reply).data(), (*reply).size());

	return isAliveResponse.isalive();
}

std::vector<application::Configuration> Server::getApplicationConfigurations() const {

	vector<application::Configuration> configVector;

	string strRequestType = m_impl->createRequestType(PROTO_ALLAVAILABLE);
	string strRequestData = m_impl->createAllAvailableRequest();
	unique_ptr<zmq::message_t> reply = m_impl->tryRequestWithOnePartReply(strRequestType, strRequestData, m_serverEndpoint);

	proto::AllAvailableResponse allAvailableResponse;
	allAvailableResponse.ParseFromArray((*reply).data(), (*reply).size());

	for (int i = 0; i < allAvailableResponse.applicationconfig_size(); ++i) {
		proto::ApplicationConfig config = allAvailableResponse.applicationconfig(i);

		application::Configuration applicationConfig(config.name(),
				config.description(),
				config.runssingle(),
				config.restart(),
				config.startingtime(),
				config.retries(),
				config.stoppingtime());

		configVector.push_back(applicationConfig);
	}

	return configVector;
}

std::vector<application::Info> Server::getApplicationInfos() const {

	vector<application::Info> infoVector;

	string strRequestType = m_impl->createRequestType(PROTO_SHOWALL);
	string strRequestData = m_impl->createShowAllRequest();
	unique_ptr<zmq::message_t> reply = m_impl->tryRequestWithOnePartReply(strRequestType, strRequestData, m_serverEndpoint);

	proto::ApplicationInfoListResponse response;
	response.ParseFromArray((*reply).data(), (*reply).size());

	for (int i = 0; i < response.applicationinfo_size(); ++i) {
		proto::ApplicationInfo info = response.applicationinfo(i);

		application::Info applicationInfo(info.name(),
						info.id(),
						info.pid(),
						info.applicationstate(),
						info.pastapplicationstates(),
						info.args());

		infoVector.push_back(applicationInfo);
	}

	return infoVector;
}

std::vector<application::Info> Server::getApplicationInfos(const std::string& name) const {

	vector<application::Info> allInfoVector = getApplicationInfos();
	vector<application::Info> infoVector;

	for (vector<application::Info>::const_iterator i = allInfoVector.begin(); i != allInfoVector.end(); ++i) {
		application::Info const & info = *i;
		if (info.getName() == name) {
			infoVector.push_back(info);
		}
	}

	return infoVector;
}

std::unique_ptr<EventStreamSocket> Server::openEventStream() {
	return Services::openEventStream();
}

std::unique_ptr<application::Subscriber> Server::createSubscriber(int id, const std::string& publisherName, const std::string& instanceName) const {

	string strRequestType = m_impl->createRequestType(PROTO_CONNECTPUBLISHER);
	string strRequestData = m_impl->createConnectPublisherRequest(id, publisherName);

	unique_ptr<zmq::message_t> reply = m_impl->tryRequestWithOnePartReply(strRequestType, strRequestData, m_serverEndpoint);
	proto::PublisherResponse requestResponse;
	requestResponse.ParseFromArray((*reply).data(), (*reply).size());

	int publisherPort = requestResponse.publisherport();
	if (publisherPort == -1) {
		throw SubscriberCreationException(requestResponse.message());
	}

	int synchronizerPort = requestResponse.synchronizerport();
	int numberOfSubscribers = requestResponse.numberofsubscribers();

	unique_ptr<application::Subscriber> subscriber(new application::Subscriber(this, getUrl(), publisherPort, synchronizerPort, publisherName, numberOfSubscribers, instanceName, id, m_serverEndpoint, m_serverStatusEndpoint));
	subscriber->init();

	return subscriber;
}

std::unique_ptr<ConnectionChecker> Server::createConnectionChecker(ConnectionCheckerType handler, int pollingTimeMs) {

	unique_ptr<ConnectionChecker> connectionChecker(new ConnectionChecker(this, handler));
	connectionChecker->startThread(getAvailableTimeout(), pollingTimeMs);

	return connectionChecker;
}

std::vector<EventListener *> Server::getEventListeners() {
	std::unique_lock<std::mutex> lock(m_eventListenersMutex);
	return m_eventListeners;
}

void Server::registerEventListener(EventListener * listener) {
	std::unique_lock<std::mutex> lock(m_eventListenersMutex);
	m_eventListeners.push_back(listener);
}

void Server::unregisterEventListener(EventListener * listener) {
	std::unique_lock<std::mutex> lock(m_eventListenersMutex);

	// Iterate to find the listener.
	for (auto it = m_eventListeners.begin(); it != m_eventListeners.end(); ++it) {
		if (*it == listener) {
			m_eventListeners.erase(it);
			break;
		}
	}
}

std::ostream& operator<<(std::ostream& os, const cameo::Server& server) {

	os << "server@" << server.m_serverEndpoint;

	return os;
}

}
