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
#include "impl/ServicesImpl.h"
#include "EventThread.h"
#include "impl/StreamSocketImpl.h"
#include "impl/RequestSocketImpl.h"
#include "message/Message.h"
#include "UndefinedApplicationException.h"
#include "UndefinedKeyException.h"
#include <iostream>
#include <sstream>
#include "JSON.h"

using namespace std;

namespace cameo {

Server::Server(const std::string& endpoint, int timeoutMs) :
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

	// Set the timeout.
	Services::setTimeout(timeoutMs);

	// Create the request socket. The server endpoint has been defined.
	Services::initRequestSocket();

	// Retrieve the server version.
	Services::retrieveServerVersion();

	// Manage the ConnectionTimeout exception that can occur.
	try {
		// Start the event thread.
		unique_ptr<EventStreamSocket> socket = openEventStream();
		m_eventThread.reset(new EventThread(this, socket));
		m_eventThread->start();
	}
	catch (...) {
		// ...
	}
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

std::array<int, 3> Server::getVersion() const {
	return Services::getVersion();
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

	unique_ptr<zmq::message_t> reply = m_requestSocket->request(m_impl->createOutputPortRequest(name));

	// Get the JSON response.
	json::Object response;
	json::parse(response, reply.get());

	return response[message::RequestResponse::VALUE].GetInt();
}

std::unique_ptr<application::Instance> Server::start(const std::string& name, const std::vector<std::string> & args, Option options) {

	bool outputStream = ((options & OUTPUTSTREAM) != 0);

	unique_ptr<application::Instance> instance = makeInstance();

	// Set the name and register the instance as event listener.
	instance->setName(name);
	registerEventListener(instance.get());

	try {
		if (outputStream) {
			// We connect to the stream port before starting the application
			// so that we are sure that the ENDSTREAM message will be received even if the application terminates rapidly.
			unique_ptr<OutputStreamSocket> socket = createOutputStreamSocket(getStreamPort(name));
			instance->setOutputStreamSocket(socket);
		}

		unique_ptr<zmq::message_t> reply = m_requestSocket->request(m_impl->createStartRequest(name, args, application::This::getReference()));

		// Get the JSON response.
		json::Object response;
		json::parse(response, reply.get());

		int value = response[message::RequestResponse::VALUE].GetInt();
		if (value == -1) {
			instance->setErrorMessage(response[message::RequestResponse::MESSAGE].GetString());
		}
		else {
			instance->setId(value);
		}
	}
	catch (const ConnectionTimeout& e) {
		instance->setErrorMessage(e.what());
	}

	return instance;
}

Response Server::stopApplicationAsynchronously(int id, bool immediately) const {

	string request;

	if (immediately) {
		request = m_impl->createKillRequest(id);
	}
	else {
		request = m_impl->createStopRequest(id);
	}

	unique_ptr<zmq::message_t> reply = m_requestSocket->request(request);

	// Get the JSON response.
	json::Object response;
	json::parse(response, reply.get());

	int value = response[message::RequestResponse::VALUE].GetInt();
	string message = response[message::RequestResponse::MESSAGE].GetString();

	return Response(value, message);
}

application::InstanceArray Server::connectAll(const std::string& name, Option options) {

	bool outputStream = ((options & OUTPUTSTREAM) != 0);

	application::InstanceArray instances;

	unique_ptr<zmq::message_t> reply = m_requestSocket->request(m_impl->createConnectRequest(name));

	// Get the JSON response.
	json::Object response;
	json::parse(response, reply.get());

	json::Value& applicationInfo = response[message::ApplicationInfoListResponse::APPLICATION_INFO];
	json::Value::Array array = applicationInfo.GetArray();
	size_t size = array.Size();

	// Allocate the array.
	instances.allocate(size);

	int aliveInstancesCount = 0;

	for (int i = 0; i < size; ++i) {
		json::Value::Object info = array[i].GetObject();

		unique_ptr<application::Instance> instance = makeInstance();

		// Set the name and register the instance as event listener.
		string name = info[message::ApplicationInfo::NAME].GetString();
		instance->setName(name);
		registerEventListener(instance.get());

		int applicationId = info[message::ApplicationInfo::ID].GetInt();

		// test if the application is still alive otherwise we could have missed a status message
		if (isAlive(applicationId)) {
			aliveInstancesCount++;

			// We connect to the stream port before starting the application
			// so that we are sure that the ENDSTREAM message will be received even if the application terminates rapidly.
			if (outputStream) {
				unique_ptr<OutputStreamSocket> socket = createOutputStreamSocket(getStreamPort(name));
				instance->setOutputStreamSocket(socket);
			}

			instance->setId(applicationId);
			instance->setInitialState(info[message::ApplicationInfo::APPLICATION_STATE].GetInt());
			instance->setPastStates(info[message::ApplicationInfo::PAST_APPLICATION_STATES].GetInt());

			instances.m_array[i] = std::move(instance);
		}
	}

	// Copy the alive instances.
	application::InstanceArray aliveInstances;
	aliveInstances.allocate(aliveInstancesCount);

	int j = 0;
	for (int i = 0; i < size; ++i) {

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

	unique_ptr<zmq::message_t> reply = m_requestSocket->request(m_impl->createIsAliveRequest(id));

	// Get the JSON response.
	json::Object response;
	json::parse(response, reply.get());

	return response[message::IsAliveResponse::IS_ALIVE].GetBool();
}

std::vector<application::Configuration> Server::getApplicationConfigurations() const {

	vector<application::Configuration> configs;

	unique_ptr<zmq::message_t> reply = m_requestSocket->request(m_impl->createListRequest());

	// Get the JSON response.
	json::Object response;
	json::parse(response, reply.get());

	json::Value& applicationConfigs = response[message::AllAvailableResponse::APPLICATION_CONFIG];
	json::Value::Array array = applicationConfigs.GetArray();
	size_t size = array.Size();

	for (int i = 0; i < size; ++i) {
		json::Value::Object config = array[i].GetObject();

		string name = config[message::ApplicationConfig::NAME].GetString();
		string description = config[message::ApplicationConfig::DESCRIPTION].GetString();
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

	vector<application::Info> infos;

	unique_ptr<zmq::message_t> reply = m_requestSocket->request(m_impl->createAppsRequest());

	// Get the JSON response.
	json::Object response;
	json::parse(response, reply.get());

	json::Value& applicationInfo = response[message::ApplicationInfoListResponse::APPLICATION_INFO];
	json::Value::Array array = applicationInfo.GetArray();
	size_t size = array.Size();

	for (int i = 0; i < size; ++i) {
		json::Value::Object info = array[i].GetObject();

		string name = info[message::ApplicationInfo::NAME].GetString();
		int id = info[message::ApplicationInfo::ID].GetInt();
		int pid = info[message::ApplicationInfo::PID].GetInt();
		application::State state = info[message::ApplicationInfo::APPLICATION_STATE].GetInt();
		application::State pastStates = info[message::ApplicationInfo::PAST_APPLICATION_STATES].GetInt();
		string args = info[message::ApplicationInfo::ARGS].GetString();

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

	vector<application::Info> allInfos = getApplicationInfos();
	vector<application::Info> infos;

	for (vector<application::Info>::const_iterator i = allInfos.begin(); i != allInfos.end(); ++i) {
		application::Info const & info = *i;
		if (info.getName() == name) {
			infos.push_back(info);
		}
	}

	return infos;
}

std::unique_ptr<EventStreamSocket> Server::openEventStream() {
	return Services::openEventStream();
}

std::unique_ptr<application::Subscriber> Server::createSubscriber(int id, const std::string& publisherName, const std::string& instanceName) {

	unique_ptr<zmq::message_t> reply = m_requestSocket->request(m_impl->createConnectPublisherRequest(id, publisherName));

	// Get the JSON response.
	json::Object response;
	json::parse(response, reply.get());

	int publisherPort = response[message::PublisherResponse::PUBLISHER_PORT].GetInt();
	if (publisherPort == -1) {
		throw SubscriberCreationException(response[message::PublisherResponse::MESSAGE].GetString());
	}

	int synchronizerPort = response[message::PublisherResponse::SYNCHRONIZER_PORT].GetInt();
	int numberOfSubscribers = response[message::PublisherResponse::NUMBER_OF_SUBSCRIBERS].GetInt();

	unique_ptr<application::Subscriber> subscriber(new application::Subscriber(this, getUrl(), publisherPort, synchronizerPort, publisherName, numberOfSubscribers, instanceName, id, m_serverEndpoint, m_serverStatusEndpoint));
	subscriber->init();

	return subscriber;
}

std::unique_ptr<ConnectionChecker> Server::createConnectionChecker(ConnectionCheckerType handler, int pollingTimeMs) {

	unique_ptr<ConnectionChecker> connectionChecker(new ConnectionChecker(this, handler));
	connectionChecker->startThread(getAvailableTimeout(), pollingTimeMs);

	return connectionChecker;
}

void Server::storeKeyValue(int id, const std::string& key, const std::string& value) {

	unique_ptr<zmq::message_t> reply = m_requestSocket->request(m_impl->createStoreKeyValueRequest(id, key, value));

	// Get the JSON response.
	json::Object response;
	json::parse(response, reply.get());
}

std::string Server::getKeyValue(int id, const std::string& key) {

	unique_ptr<zmq::message_t> reply = m_requestSocket->request(m_impl->createGetKeyValueRequest(id, key));

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

	unique_ptr<zmq::message_t> reply = m_requestSocket->request(m_impl->createRemoveKeyRequest(id, key));

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
