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
#include "KeyAlreadyExistsException.h"
#include "EventThread.h"
#include "ImplFactory.h"
#include "impl/StreamSocketImpl.h"
#include "impl/StreamSocketImpl.h"
#include "Context.h"
#include "JSON.h"
#include "Messages.h"
#include "RequestSocket.h"
#include <iostream>
#include <sstream>
#include <stdexcept>

namespace cameo {

constexpr int DEFAULT_TIMEOUT = 10000;

const std::string Server::CAMEO_SERVER = "0:0";

EventListener * Server::FilteredEventListener::getListener() const {
	return m_listener;
}

bool Server::FilteredEventListener::isFiltered() const {
	return m_filtered;
}

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

Server::Server(const Endpoint& endpoint, int timeoutMs, bool useProxy) :
	m_timeout(0),
	m_useProxy(useProxy),
	m_statusPort(0),
	m_context(nullptr) {

	m_serverVersion[0] = 0;
	m_serverVersion[1] = 0;
	m_serverVersion[2] = 0;

	initContext();

	initServer(endpoint, timeoutMs);
}

Server::Server(const std::string& endpoint, int timeoutMs, bool useProxy) :
	m_timeout(0),
	m_useProxy(useProxy),
	m_statusPort(0),
	m_context(nullptr) {

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
	terminate();
}

void Server::terminate() {

	// Stop the event thread.
	if (m_eventThread) {
		m_eventThread->cancel();
	}
	m_eventThread.reset();

	// Reset the request socket before the impl, otherwise reset context will block.
	m_requestSocket.reset();

	// Reset the context.
	m_context.reset();
}

void Server::setTimeout(int timeout) {

	m_timeout = timeout;

	if (m_requestSocket) {
		m_requestSocket->setTimeout(timeout);
	}
}

int Server::getTimeout() const {
	return m_timeout;
}

Endpoint Server::getEndpoint() const {
	return m_serverEndpoint;
}

Endpoint Server::getStatusEndpoint() const {
	return m_serverEndpoint.withPort(m_statusPort);
}

int Server::getResponderProxyPort() const {
	return m_responderProxyPort;
}

int Server::getPublisherProxyPort() const {
	return m_publisherProxyPort;
}

int Server::getSubscriberProxyPort() const {
	return m_subscriberProxyPort;
}

std::array<int, 3> Server::getVersion() const {
	return m_serverVersion;
}

bool Server::usesProxy() const {
	return m_useProxy;
}

bool Server::isAvailable(int timeout) const {

	try {
		m_requestSocket->requestJSON(createSyncRequest(), timeout);
		return true;
	}
	catch (const ConnectionTimeout&) {
		// The server is not accessible.
	}

	return false;
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
		return DEFAULT_TIMEOUT;
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
	bool linked = ((options & UNLINKED) == 0);

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

		json::Object response;

		if (application::This::getId() == -1) {
			response = m_requestSocket->requestJSON(createStartRequest(name, args, "", 0, "", 0, false));
		}
		else {
			response = m_requestSocket->requestJSON(createStartRequest(name, args, application::This::getName(), application::This::getId(), application::This::getEndpoint().toString(), application::This::getServer().m_responderProxyPort, linked));
		}

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

Response Server::stop(int id, bool immediately) const {

	std::string request;

	if (immediately) {
		request = createKillRequest(id);
	}
	else {
		request = createStopRequest(id);
	}

	json::Object response = m_requestSocket->requestJSON(request);

	int value = response[message::RequestResponse::VALUE].GetInt();
	std::string message = response[message::RequestResponse::MESSAGE].GetString();

	return Response(value, message);
}

application::InstanceArray Server::connectAll(const std::string& name, int options) {

	bool outputStream = ((options & OUTPUTSTREAM) != 0);

	json::Object response = m_requestSocket->requestJSON(createConnectRequest(name));

	application::InstanceArray instances;

	json::Value& applicationInfo = response[message::ApplicationInfoListResponse::APPLICATION_INFO];
	json::Value::Array array = applicationInfo.GetArray();
	size_t size = array.Size();

	// Allocate the array.
	instances.reserve(size);

	int aliveInstancesCount = 0;

	for (size_t i = 0; i < size; ++i) {
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

	for (size_t i = 0; i < size; ++i) {

		if (instances[i].get() != nullptr) {
			aliveInstances.push_back(std::move(instances[i]));
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

	json::Object response = m_requestSocket->requestJSON(createConnectWithIdRequest(id));

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

	for (auto i = instances.begin(); i != instances.end(); ++i) {
		(*i)->kill();
		(*i)->waitFor();
	}
}

bool Server::isAlive(int id) const {

	json::Object response = m_requestSocket->requestJSON(createIsAliveRequest(id));

	return response[message::IsAliveResponse::IS_ALIVE].GetBool();
}

std::vector<application::Configuration> Server::getApplicationConfigurations() const {

	std::vector<application::Configuration> configs;

	json::Object response = m_requestSocket->requestJSON(createListRequest());

	json::Value& applicationConfigs = response[message::ApplicationConfigListResponse::APPLICATION_CONFIG];
	json::Value::Array array = applicationConfigs.GetArray();
	size_t size = array.Size();

	for (size_t i = 0; i < size; ++i) {
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

	json::Object response = m_requestSocket->requestJSON(createAppsRequest());

	json::Value& applicationInfos = response[message::ApplicationInfoListResponse::APPLICATION_INFO];
	json::Value::Array array = applicationInfos.GetArray();
	size_t size = array.Size();

	for (size_t i = 0; i < size; ++i) {
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

	json::Object response = m_requestSocket->requestJSON(createPortsRequest());

	json::Value& portInfos = response[message::PortInfoListResponse::PORT_INFO];
	json::Value::Array array = portInfos.GetArray();
	size_t size = array.Size();

	for (size_t i = 0; i < size; ++i) {
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

	json::Object response = m_requestSocket->requestJSON(createGetStatusRequest(id));

	return response[message::StatusEvent::APPLICATION_STATE].GetInt();
}

std::set<application::State> Server::getPastStates(int id) const {

	json::Object response = m_requestSocket->requestJSON(createGetStatusRequest(id));

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

		if (m_useProxy) {
			// With the proxy, the status port is the publisher proxy port.
			m_statusPort = m_publisherProxyPort;
		}
		else {
			m_statusPort = m_serverStatusPort;
		}
	}

	// Create the event stream socket.
	std::unique_ptr<EventStreamSocket> eventStreamSocket = std::unique_ptr<EventStreamSocket>(new EventStreamSocket());
	eventStreamSocket->init(m_context.get(), m_serverEndpoint.withPort(m_statusPort), m_requestSocket.get());

	return eventStreamSocket;
}

std::unique_ptr<ConnectionChecker> Server::createConnectionChecker(ConnectionCheckerType handler, int pollingTimeMs) {

	std::unique_ptr<ConnectionChecker> connectionChecker(new ConnectionChecker(this, handler));
	connectionChecker->startThread(getAvailableTimeout(), pollingTimeMs);

	return connectionChecker;
}

void Server::storeKeyValue(int id, const std::string& key, const std::string& value) {

	json::Object response = m_requestSocket->requestJSON(createStoreKeyValueRequest(id, key, value));

	int responseValue = response[message::RequestResponse::VALUE].GetInt();
	if (responseValue == -1) {
		throw UndefinedApplicationException(response[message::RequestResponse::MESSAGE].GetString());
	}
	else if (responseValue == -2) {
		throw KeyAlreadyExistsException(response[message::RequestResponse::MESSAGE].GetString());
	}
}

std::string Server::getKeyValue(int id, const std::string& key) {

	json::Object response = m_requestSocket->requestJSON(createGetKeyValueRequest(id, key));

	int responseValue = response[message::RequestResponse::VALUE].GetInt();
	if (responseValue == 0) {
		return response[message::RequestResponse::MESSAGE].GetString();
	}
	else if (responseValue == -1) {
		throw UndefinedApplicationException(response[message::RequestResponse::MESSAGE].GetString());
	}
	else if (responseValue == -2) {
		throw UndefinedKeyException(response[message::RequestResponse::MESSAGE].GetString());
	}

	return "";
}

void Server::removeKey(int id, const std::string& key) {

	json::Object response = m_requestSocket->requestJSON(createRemoveKeyRequest(id, key));

	int value = response[message::RequestResponse::VALUE].GetInt();
	if (value == -1) {
		throw UndefinedApplicationException(response[message::RequestResponse::MESSAGE].GetString());
	}
	else if (value == -2) {
		throw UndefinedKeyException(response[message::RequestResponse::MESSAGE].GetString());
	}
}

int Server::requestPort(int id) {

	json::Object response = m_requestSocket->requestJSON(createRequestPortRequest(id));

	int value = response[message::RequestResponse::VALUE].GetInt();
	if (value == -1) {
		throw UndefinedApplicationException(response[message::RequestResponse::MESSAGE].GetString());
	}

	return value;
}

void Server::setPortUnavailable(int id, int port) {

	json::Object response = m_requestSocket->requestJSON(createPortUnavailableRequest(id, port));

	int value = response[message::RequestResponse::VALUE].GetInt();
	if (value == -1) {
		throw UndefinedApplicationException(response[message::RequestResponse::MESSAGE].GetString());
	}
}

void Server::releasePort(int id, int port) {

	json::Object response = m_requestSocket->requestJSON(createReleasePortRequest(id, port));

	int value = response[message::RequestResponse::VALUE].GetInt();
	if (value == -1) {
		throw UndefinedApplicationException(response[message::RequestResponse::MESSAGE].GetString());
	}
}

json::Object Server::requestJSON(const std::string& request, int overrideTimeout) {
	return m_requestSocket->requestJSON(request, overrideTimeout);
}

json::Object Server::requestJSON(const std::string& requestPart1, const std::string& requestPart2, int overrideTimeout) {
	return m_requestSocket->requestJSON(requestPart1, requestPart2, overrideTimeout);
}

std::vector<Server::FilteredEventListener> Server::getEventListeners() {
	std::unique_lock<std::mutex> lock(m_eventListenersMutex);
	return m_eventListeners;
}

void Server::registerEventListener(EventListener * listener, bool filtered) {
	std::unique_lock<std::mutex> lock(m_eventListenersMutex);
	m_eventListeners.push_back(FilteredEventListener(listener, filtered));
}

void Server::unregisterEventListener(EventListener * listener) {
	std::unique_lock<std::mutex> lock(m_eventListenersMutex);

	// Iterate to find the listener.
	for (auto it = m_eventListeners.begin(); it != m_eventListeners.end(); ++it) {
		if (it->getListener() == listener) {
			m_eventListeners.erase(it);
			break;
		}
	}
}

void Server::initContext() {
	// Set the impl.
	m_context = ImplFactory::createContext();
}

void Server::initRequestSocket() {

	// Create the request socket. The server endpoint must have been initialized.
	m_requestSocket = std::move(createRequestSocket(m_serverEndpoint.toString(), CAMEO_SERVER, m_timeout));

	// Get the status port.
	m_serverStatusPort = retrieveStatusPort();

	// Get the proxy ports.
	m_responderProxyPort = retrieveResponderProxyPort();
	m_publisherProxyPort = retrievePublisherProxyPort();
	m_subscriberProxyPort = retrieveSubscriberProxyPort();
}

void Server::retrieveServerVersion() {

	json::Object response = m_requestSocket->requestJSON(createVersionRequest());

	m_serverVersion[0] = response[message::VersionResponse::MAJOR].GetInt();
	m_serverVersion[1] = response[message::VersionResponse::MINOR].GetInt();
	m_serverVersion[2] = response[message::VersionResponse::REVISION].GetInt();
}

int Server::retrieveStatusPort() {

	json::Object response = m_requestSocket->requestJSON(createStreamStatusRequest());

	return response[message::RequestResponse::VALUE].GetInt();
}

int Server::retrieveStreamPort(const std::string& name) {

	json::Object response = m_requestSocket->requestJSON(createOutputPortRequest(name));

	return response[message::RequestResponse::VALUE].GetInt();
}

int Server::retrieveResponderProxyPort() {

	json::Object response = m_requestSocket->requestJSON(createResponderProxyPortRequest());

	return response[message::RequestResponse::VALUE].GetInt();
}

int Server::retrievePublisherProxyPort() {

	json::Object response = m_requestSocket->requestJSON(createPublisherProxyPortRequest());

	return response[message::RequestResponse::VALUE].GetInt();
}

int Server::retrieveSubscriberProxyPort() {

	json::Object response = m_requestSocket->requestJSON(createSubscriberProxyPortRequest());

	return response[message::RequestResponse::VALUE].GetInt();
}

std::unique_ptr<OutputStreamSocket> Server::createOutputStreamSocket(const std::string& name) {

	// Create the output stream socket.
	std::unique_ptr<OutputStreamSocket> outputStreamSocket = std::unique_ptr<OutputStreamSocket>(new OutputStreamSocket(name));

	// Even with the proxy, it is necessary to check if the application has an output stream.
	int port = retrieveStreamPort(name);
	if (port == -1) {
		std::cerr << "No stream port for " << name << std::endl;
		return nullptr;
	}

	// With the proxy, the port is the publisher proxy port i.e. the status port.
	if (m_useProxy) {
		port = m_statusPort;
	}

	outputStreamSocket->init(m_context.get(), m_serverEndpoint.withPort(port), m_requestSocket.get());

	return outputStreamSocket;
}

std::unique_ptr<RequestSocket> Server::createRequestSocket(const std::string& endpoint, const std::string& responderIdentity) {
	return std::unique_ptr<RequestSocket>(new RequestSocket(m_context.get(), endpoint, responderIdentity, m_timeout));
}

std::unique_ptr<RequestSocket> Server::createRequestSocket(const std::string& endpoint, const std::string& responderIdentity, int timeout) {
	return std::unique_ptr<RequestSocket>(new RequestSocket(m_context.get(), endpoint, responderIdentity, timeout));
}

std::ostream& operator<<(std::ostream& os, const cameo::Server& server) {

	os << "server@" << server.m_serverEndpoint.toString();

	return os;
}

}
