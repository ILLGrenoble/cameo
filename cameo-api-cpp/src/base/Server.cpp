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

#include "Application.h"
#include "ConnectionChecker.h"
#include "UndefinedApplicationException.h"
#include "UndefinedKeyException.h"
#include "EventThread.h"
#include "impl/CancelIdGenerator.h"
#include "impl/RequestSocketImpl.h"
#include "impl/StreamSocketImpl.h"
#include "impl/ContextImpl.h"
#include "JSON.h"
#include "Messages.h"
#include <iostream>
#include <sstream>
#include <stdexcept>

namespace cameo {
constexpr int defaultTimeout = 10000;
	
void Server::initServer(const Endpoint& endpoint, int timeoutMs) {

	initContext();

	m_serverEndpoint = endpoint;

	// Set the timeout.
	setTimeout(timeoutMs);

	// Create the request socket. The server endpoint has been defined.
	initRequestSocket();

	// Manage the ConnectionTimeout exception that can occur.
	try {
		// Retrieve the server version.
		retrieveServerVersion();

		// Start the event thread.
		std::unique_ptr<EventStreamSocket> socket = openEventStream();
		m_eventThread.reset(new EventThread(this, socket));
		m_eventThread->start();
	}
	catch (...) {
		// ...
	}
}

Server::Server(const Endpoint& endpoint, int timeoutMs) :
	m_statusPort(0),
	m_contextImpl(nullptr) {

	m_serverVersion[0] = 0;
	m_serverVersion[1] = 0;
	m_serverVersion[2] = 0;

	initContext();

	initServer(endpoint, timeoutMs);
}

Server::Server(const std::string& endpoint, int timeoutMs) :
	m_statusPort(0),
	m_contextImpl(nullptr) {

	m_serverVersion[0] = 0;
	m_serverVersion[1] = 0;
	m_serverVersion[2] = 0;


	initContext();

	try {
		initServer(Endpoint::parse(endpoint), timeoutMs);
	}
	catch (...) {
		throw InvalidArgumentException(endpoint + " is not a valid endpoint");
	}
}

Server::~Server() {

	// Stop the event thread.
	if (m_eventThread.get() != nullptr) {
		m_eventThread->cancel();
	}
	m_eventThread.reset();

	// Reset the request socket before the impl, otherwise reset context will block.
	m_requestSocket.reset();

	// Reset the context.
	m_contextImpl.reset();
}

void Server::setTimeout(int timeout) {

	m_contextImpl->setTimeout(timeout);

	if (m_requestSocket.get() != nullptr) {
		m_requestSocket->setTimeout(timeout);
	}
}

int Server::getTimeout() const {
	return m_contextImpl->getTimeout();
}

const Endpoint& Server::getEndpoint() const {
	return m_serverEndpoint;
}

std::array<int, 3> Server::getVersion() const {
	return m_serverVersion;
}

bool Server::isAvailable(int timeout) const {
	return m_contextImpl->isAvailable(m_requestSocket.get(), timeout);
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
		return defaultTimeout;
	}
}

std::unique_ptr<application::Instance> Server::makeInstance() {
	return std::unique_ptr<application::Instance>(new application::Instance(this));
}

std::unique_ptr<application::Instance> Server::start(const std::string& name, int options) {
	return start(name, std::vector<std::string>(), options);
}

std::unique_ptr<application::Instance> Server::start(const std::string& name, const std::vector<std::string> & args, int options) {

	bool outputStream = ((options & OUTPUTSTREAM) != 0);

	std::unique_ptr<application::Instance> instance = makeInstance();

	// Set the name and register the instance as event listener.
	instance->setName(name);
	registerEventListener(instance.get());

	try {
		std::unique_ptr<OutputStreamSocket> streamSocket;

		if (outputStream) {
			// Connect to the stream port. A sync is made to ensure that the subscriber is connected.
			streamSocket = createOutputStreamSocket(name);
		}

		std::unique_ptr<zmq::message_t> reply = m_requestSocket->request(createStartRequest(name, args, application::This::getName(), application::This::getId(), application::This::getEndpoint().toString()));

		// Get the JSON response.
		json::Object response;
		json::parse(response, reply.get());

		int value = response[message::RequestResponse::VALUE].GetInt();
		if (value == -1) {
			instance->setErrorMessage(response[message::RequestResponse::MESSAGE].GetString());
		}
		else {
			instance->setId(value);

			if (outputStream) {
				instance->setOutputStreamSocket(streamSocket);
			}
		}
	}
	catch (const ConnectionTimeout& e) {
		instance->setErrorMessage(e.what());
	}

	return instance;
}

Response Server::stopApplicationAsynchronously(int id, bool immediately) const {

	std::string request;

	if (immediately) {
		request = createKillRequest(id);
	}
	else {
		request = createStopRequest(id);
	}

	std::unique_ptr<zmq::message_t> reply = m_requestSocket->request(request);

	// Get the JSON response.
	json::Object response;
	json::parse(response, reply.get());

	int value = response[message::RequestResponse::VALUE].GetInt();
	std::string message = response[message::RequestResponse::MESSAGE].GetString();

	return Response(value, message);
}

application::InstanceArray Server::connectAll(const std::string& name, int options) {

	bool outputStream = ((options & OUTPUTSTREAM) != 0);

	std::unique_ptr<zmq::message_t> reply = m_requestSocket->request(createConnectRequest(name));

	// Get the JSON response.
	json::Object response;
	json::parse(response, reply.get());

	application::InstanceArray instances;

	json::Value& applicationInfo = response[message::ApplicationInfoListResponse::APPLICATION_INFO];
	json::Value::Array array = applicationInfo.GetArray();
	size_t size = array.Size();

	// Allocate the array.
	instances.reserve(size);

	int aliveInstancesCount = 0;

	for (int i = 0; i < size; ++i) {
		json::Value::Object info = array[i].GetObject();

		std::unique_ptr<application::Instance> instance = makeInstance();

		// Set the name and register the instance as event listener.
		std::string name = info[message::ApplicationInfo::NAME].GetString();
		instance->setName(name);
		registerEventListener(instance.get());

		int applicationId = info[message::ApplicationInfo::ID].GetInt();

		// test if the application is still alive otherwise we could have missed a status message
		if (isAlive(applicationId)) {
			aliveInstancesCount++;

			instance->setId(applicationId);
			instance->setInitialState(info[message::ApplicationInfo::APPLICATION_STATE].GetInt());
			instance->setPastStates(info[message::ApplicationInfo::PAST_APPLICATION_STATES].GetInt());

			if (outputStream) {
				std::unique_ptr<OutputStreamSocket> streamSocket = createOutputStreamSocket(name);
				instance->setOutputStreamSocket(streamSocket);
			}

			instances.push_back(std::move(instance));
		}
	}

	// Copy the alive instances.
	application::InstanceArray aliveInstances;
	aliveInstances.reserve(aliveInstancesCount);

	int j = 0;
	for (int i = 0; i < size; ++i) {

		if (instances[i].get() != nullptr) {
			aliveInstances.push_back(std::move(instances[i]));
			j++;
		}
	}

	return aliveInstances;
}

std::unique_ptr<application::Instance> Server::connect(const std::string& name, int options) {

	application::InstanceArray instances = connectAll(name, options);

	if (instances.size() == 0) {
		std::unique_ptr<application::Instance> instance = makeInstance();
		return instance;
	}

	return std::move(instances[0]);
}

std::unique_ptr<application::Instance> Server::connect(int id, int options) {

	bool outputStream = ((options & OUTPUTSTREAM) != 0);

	std::unique_ptr<zmq::message_t> reply = m_requestSocket->request(createConnectWithIdRequest(id));

	// Get the JSON response.
	json::Object response;
	json::parse(response, reply.get());

	json::Value& applicationInfo = response[message::ApplicationInfoListResponse::APPLICATION_INFO];
	json::Value::Array array = applicationInfo.GetArray();
	size_t size = array.Size();

	if (size > 0) {
		json::Value::Object info = array[0].GetObject();

		std::unique_ptr<application::Instance> instance = makeInstance();

		// Set the name and register the instance as event listener.
		std::string name = info[message::ApplicationInfo::NAME].GetString();
		instance->setName(name);
		registerEventListener(instance.get());

		int applicationId = info[message::ApplicationInfo::ID].GetInt();

		// test if the application is still alive otherwise we could have missed a status message
		if (isAlive(applicationId)) {

			instance->setId(applicationId);
			instance->setInitialState(info[message::ApplicationInfo::APPLICATION_STATE].GetInt());
			instance->setPastStates(info[message::ApplicationInfo::PAST_APPLICATION_STATES].GetInt());

			if (outputStream) {
				std::unique_ptr<OutputStreamSocket> streamSocket = createOutputStreamSocket(name);
				instance->setOutputStreamSocket(streamSocket);
			}

			return instance;
		}
	}

	return makeInstance();
}

void Server::killAllAndWaitFor(const std::string& name) {

	application::InstanceArray instances = connectAll(name);

	for (int i = 0; i < instances.size(); ++i) {
		instances[i]->kill();
		instances[i]->waitFor();
	}
}

bool Server::isAlive(int id) const {

	std::unique_ptr<zmq::message_t> reply = m_requestSocket->request(createIsAliveRequest(id));

	// Get the JSON response.
	json::Object response;
	json::parse(response, reply.get());

	return response[message::IsAliveResponse::IS_ALIVE].GetBool();
}

std::vector<application::Configuration> Server::getApplicationConfigurations() const {

	std::vector<application::Configuration> configs;

	std::unique_ptr<zmq::message_t> reply = m_requestSocket->request(createListRequest());

	// Get the JSON response.
	json::Object response;
	json::parse(response, reply.get());

	json::Value& applicationConfigs = response[message::ApplicationConfigListResponse::APPLICATION_CONFIG];
	json::Value::Array array = applicationConfigs.GetArray();
	size_t size = array.Size();

	for (int i = 0; i < size; ++i) {
		json::Value::Object config = array[i].GetObject();

		std::string name = config[message::ApplicationConfig::NAME].GetString();
		std::string description = config[message::ApplicationConfig::DESCRIPTION].GetString();
		bool runsSingle = config[message::ApplicationConfig::RUNS_SINGLE].GetBool();
		bool restart = config[message::ApplicationConfig::RESTART].GetBool();
		int startingTime = config[message::ApplicationConfig::STARTING_TIME].GetInt();
		int stoppingTime = config[message::ApplicationConfig::STOPPING_TIME].GetInt();

		application::Configuration applicationConfig(name,
				description,
				runsSingle,
				restart,
				startingTime,
				stoppingTime);

		configs.push_back(applicationConfig);
	}

	return configs;
}

std::vector<application::Info> Server::getApplicationInfos() const {

	std::vector<application::Info> infos;

	std::unique_ptr<zmq::message_t> reply = m_requestSocket->request(createAppsRequest());

	// Get the JSON response.
	json::Object response;
	json::parse(response, reply.get());

	json::Value& applicationInfos = response[message::ApplicationInfoListResponse::APPLICATION_INFO];
	json::Value::Array array = applicationInfos.GetArray();
	size_t size = array.Size();

	for (int i = 0; i < size; ++i) {
		json::Value::Object info = array[i].GetObject();

		std::string name = info[message::ApplicationInfo::NAME].GetString();
		int id = info[message::ApplicationInfo::ID].GetInt();
		int pid = info[message::ApplicationInfo::PID].GetInt();
		application::State state = info[message::ApplicationInfo::APPLICATION_STATE].GetInt();
		application::State pastStates = info[message::ApplicationInfo::PAST_APPLICATION_STATES].GetInt();
		std::string args = info[message::ApplicationInfo::ARGS].GetString();

		application::Info applicationInfo(name,
						id,
						pid,
						state,
						pastStates,
						args);

		infos.push_back(applicationInfo);
	}

	return infos;
}

std::vector<application::Info> Server::getApplicationInfos(const std::string& name) const {

	std::vector<application::Info> allInfos = getApplicationInfos();
	std::vector<application::Info> infos;

	for (std::vector<application::Info>::const_iterator i = allInfos.begin(); i != allInfos.end(); ++i) {
		application::Info const & info = *i;
		if (info.getName() == name) {
			infos.push_back(info);
		}
	}

	return infos;
}

std::vector<application::Port> Server::getPorts() const {

	std::vector<application::Port> ports;

	std::unique_ptr<zmq::message_t> reply = m_requestSocket->request(createPortsRequest());

	// Get the JSON response.
	json::Object response;
	json::parse(response, reply.get());

	json::Value& portInfos = response[message::PortInfoListResponse::PORT_INFO];
	json::Value::Array array = portInfos.GetArray();
	size_t size = array.Size();

	for (int i = 0; i < size; ++i) {
		json::Value::Object info = array[i].GetObject();

		int port = info[message::PortInfo::PORT].GetInt();
		std::string status = info[message::PortInfo::STATUS].GetString();
		std::string owner = info[message::PortInfo::OWNER].GetString();

		application::Port portInfo(port, status, owner);

		ports.push_back(portInfo);
	}

	return ports;
}

application::State Server::getActualState(int id) const {

	std::unique_ptr<zmq::message_t> reply = m_requestSocket->request(createGetStatusRequest(id));

	// Get the JSON response.
	json::Object response;
	json::parse(response, reply.get());

	return response[message::StatusEvent::APPLICATION_STATE].GetInt();
}

std::set<application::State> Server::getPastStates(int id) const {

	std::unique_ptr<zmq::message_t> reply = m_requestSocket->request(createGetStatusRequest(id));

	// Get the JSON response.
	json::Object response;
	json::parse(response, reply.get());

	application::State applicationStates = response[message::StatusEvent::PAST_APPLICATION_STATES].GetInt();

	std::set<application::State> result;

	if ((applicationStates & application::STARTING) != 0) {
		result.insert(application::STARTING);
	}

	if ((applicationStates & application::RUNNING) != 0) {
		result.insert(application::RUNNING);
	}

	if ((applicationStates & application::STOPPING) != 0) {
		result.insert(application::STOPPING);
	}

	if ((applicationStates & application::KILLING) != 0) {
		result.insert(application::KILLING);
	}

	if ((applicationStates & application::PROCESSING_ERROR) != 0) {
		result.insert(application::PROCESSING_ERROR);
	}

	if ((applicationStates & application::FAILURE) != 0) {
		result.insert(application::FAILURE);
	}

	if ((applicationStates & application::SUCCESS) != 0) {
		result.insert(application::SUCCESS);
	}

	if ((applicationStates & application::STOPPED) != 0) {
		result.insert(application::STOPPED);
	}

	if ((applicationStates & application::KILLED) != 0) {
		result.insert(application::KILLED);
	}

	return result;
}

std::unique_ptr<EventStreamSocket> Server::openEventStream() {

	// Init the status port if necessary.
	if (m_statusPort == 0) {
		initStatus();
	}

	std::stringstream cancelEndpoint;

	// We define a unique name that depends on the event stream socket object because there can be many (instances).
	cancelEndpoint << "inproc://cancel." << CancelIdGenerator::newId();

	// Create the sockets.
	zmq::socket_t * cancelPublisher = m_contextImpl->createCancelPublisher(cancelEndpoint.str());
	zmq::socket_t * subscriber = m_contextImpl->createEventSubscriber(getStatusEndpoint().toString(), cancelEndpoint.str());

	// Wait for the connection to be ready.
	m_contextImpl->waitForSubscriber(subscriber, m_requestSocket.get());

	// Create the event stream socket.
	return std::unique_ptr<EventStreamSocket>(new EventStreamSocket(new StreamSocketImpl(subscriber, cancelPublisher)));
}

std::unique_ptr<ConnectionChecker> Server::createConnectionChecker(ConnectionCheckerType handler, int pollingTimeMs) {

	std::unique_ptr<ConnectionChecker> connectionChecker(new ConnectionChecker(this, handler));
	connectionChecker->startThread(getAvailableTimeout(), pollingTimeMs);

	return connectionChecker;
}

void Server::storeKeyValue(int id, const std::string& key, const std::string& value) {

	std::unique_ptr<zmq::message_t> reply = m_requestSocket->request(createStoreKeyValueRequest(id, key, value));

	// Get the JSON response.
	json::Object response;
	json::parse(response, reply.get());
}

std::string Server::getKeyValue(int id, const std::string& key) {

	std::unique_ptr<zmq::message_t> reply = m_requestSocket->request(createGetKeyValueRequest(id, key));

	// Get the JSON response.
	json::Object response;
	json::parse(response, reply.get());

	int value = response[message::RequestResponse::VALUE].GetInt();
	if (value == 0) {
		return response[message::RequestResponse::MESSAGE].GetString();
	}
	else if (value == -1) {
		throw UndefinedApplicationException(response[message::RequestResponse::MESSAGE].GetString());
	}
	else if (value == -2) {
		throw UndefinedKeyException(response[message::RequestResponse::MESSAGE].GetString());
	}

	return "";
}

void Server::removeKey(int id, const std::string& key) {

	std::unique_ptr<zmq::message_t> reply = m_requestSocket->request(createRemoveKeyRequest(id, key));

	// Get the JSON response.
	json::Object response;
	json::parse(response, reply.get());

	int value = response[message::RequestResponse::VALUE].GetInt();
	if (value == -1) {
		throw UndefinedApplicationException(response[message::RequestResponse::MESSAGE].GetString());
	}
	else if (value == -2) {
		throw UndefinedKeyException(response[message::RequestResponse::MESSAGE].GetString());
	}
}

int Server::requestPort(int id) {

	std::unique_ptr<zmq::message_t> reply = m_requestSocket->request(createRequestPortRequest(id));

	// Get the JSON response.
	json::Object response;
	json::parse(response, reply.get());

	int value = response[message::RequestResponse::VALUE].GetInt();
	if (value == -1) {
		throw UndefinedApplicationException(response[message::RequestResponse::MESSAGE].GetString());
	}

	return value;
}

void Server::setPortUnavailable(int id, int port) {

	std::unique_ptr<zmq::message_t> reply = m_requestSocket->request(createPortUnavailableRequest(id, port));

	// Get the JSON response.
	json::Object response;
	json::parse(response, reply.get());

	int value = response[message::RequestResponse::VALUE].GetInt();
	if (value == -1) {
		throw UndefinedApplicationException(response[message::RequestResponse::MESSAGE].GetString());
	}
}

void Server::releasePort(int id, int port) {

	std::unique_ptr<zmq::message_t> reply = m_requestSocket->request(createReleasePortRequest(id, port));

	// Get the JSON response.
	json::Object response;
	json::parse(response, reply.get());

	int value = response[message::RequestResponse::VALUE].GetInt();
	if (value == -1) {
		throw UndefinedApplicationException(response[message::RequestResponse::MESSAGE].GetString());
	}
}

json::Object Server::request(const std::string& request, int overrideTimeout) {

	std::unique_ptr<zmq::message_t> reply = m_requestSocket->request(request, overrideTimeout);

	// Get the JSON response.
	json::Object response;
	json::parse(response, reply.get());

	return response;
}

json::Object Server::request(const std::string& requestPart1, const std::string& requestPart2, int overrideTimeout) {

	std::unique_ptr<zmq::message_t> reply = m_requestSocket->request(requestPart1, requestPart2, overrideTimeout);

	// Get the JSON response.
	json::Object response;
	json::parse(response, reply.get());

	return response;
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

void Server::initContext() {
	// Set the impl.
	m_contextImpl.reset(new ContextImpl());
}

void Server::initRequestSocket() {
	// Create the request socket. The server endpoint must have been initialized.
	m_requestSocket = std::move(createRequestSocket(m_serverEndpoint.toString(), m_contextImpl->getTimeout()));
}

Endpoint Server::getStatusEndpoint() const {
	return m_serverEndpoint.withPort(m_statusPort);
}

void Server::retrieveServerVersion() {

	// Get the version.
	std::unique_ptr<zmq::message_t> reply = m_requestSocket->request(createVersionRequest());

	// Get the JSON response.
	json::Object response;
	json::parse(response, reply.get());

	m_serverVersion[0] = response[message::VersionResponse::MAJOR].GetInt();
	m_serverVersion[1] = response[message::VersionResponse::MINOR].GetInt();
	m_serverVersion[2] = response[message::VersionResponse::REVISION].GetInt();
}

void Server::initStatus() {

	// Get the status port.
	std::unique_ptr<zmq::message_t> reply = m_requestSocket->request(createStreamStatusRequest());

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
}

int Server::getStreamPort(const std::string& name) {

	std::unique_ptr<zmq::message_t> reply = m_requestSocket->request(createOutputPortRequest(name));

	// Get the JSON response.
	json::Object response;
	json::parse(response, reply.get());

	return response[message::RequestResponse::VALUE].GetInt();
}

std::unique_ptr<OutputStreamSocket> Server::createOutputStreamSocket(const std::string& name) {

	int port = getStreamPort(name);

	if (port == -1) {
		return nullptr;
	}

	// We define a unique name that depends on the event stream socket object because there can be many (instances).
	std::string cancelEndpoint = "inproc://cancel." + std::to_string(CancelIdGenerator::newId());

	// Create the sockets.
	zmq::socket_t * cancelPublisher = m_contextImpl->createCancelPublisher(cancelEndpoint);
	zmq::socket_t * subscriber = m_contextImpl->createOutputStreamSubscriber(m_serverEndpoint.withPort(port).toString(), cancelEndpoint);

	// Wait for the connection to be ready.
	m_contextImpl->waitForStreamSubscriber(subscriber, m_requestSocket.get(), name);

	// Create the output stream socket.
	return std::unique_ptr<OutputStreamSocket>(new OutputStreamSocket(new StreamSocketImpl(subscriber, cancelPublisher)));
}

std::unique_ptr<RequestSocketImpl> Server::createRequestSocket(const std::string& endpoint) {
	return std::unique_ptr<RequestSocketImpl>(new RequestSocketImpl(m_contextImpl.get(), endpoint, m_contextImpl->getTimeout()));
}

std::unique_ptr<RequestSocketImpl> Server::createRequestSocket(const std::string& endpoint, int timeout) {
	return std::unique_ptr<RequestSocketImpl>(new RequestSocketImpl(m_contextImpl.get(), endpoint, timeout));
}

std::ostream& operator<<(std::ostream& os, const cameo::Server& server) {

	os << "server@" << server.m_serverEndpoint.toString();

	return os;
}

}