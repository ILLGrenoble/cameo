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

#include "Application.h"

#include "CancelEvent.h"
#include "KeyEvent.h"
#include "ResultEvent.h"
#include "StarterServerException.h"
#include "StatusEvent.h"
#include "EventStreamSocket.h"
#include "impl/HandlerImpl.h"
#include "impl/StreamSocketImpl.h"
#include "impl/zmq/ContextZmq.h"
#include "Strings.h"
#include "Server.h"
#include "Messages.h"
#include "RequestSocket.h"
#include "Waiting.h"
#include "WaitingSet.h"
#include <sstream>
#include <iostream>
#include <stdexcept>
#include <vector>

// Using Visual Studio preprocessor.
// It must be improved in case of other compilers.
#ifdef _WIN32
	#include <process.h>
	#define GET_PROCESS_PID() _getpid()
#else
	#include <unistd.h>

	#define GET_PROCESS_PID() ::getpid()
#endif

namespace cameo {
namespace application {

This This::m_instance;
const std::string This::RUNNING_STATE = "RUNNING";

This::Com::Com(Server * server, int applicationId) :
	m_server(server),
	m_applicationId(applicationId) {
}

Context* This::Com::getContext() const {
	return m_server->m_context.get();
}

void This::Com::storeKeyValue(const std::string& key, const std::string& value) const {
	m_server->storeKeyValue(m_applicationId, key, value);
}
std::string This::Com::getKeyValue(const std::string& key) const {
	return m_server->getKeyValue(m_applicationId, key);
}

void This::Com::removeKey(const std::string& key) const {
	m_server->removeKey(m_applicationId, key);
}

int This::Com::requestPort() const {
	return m_server->requestPort(m_applicationId);
}

void This::Com::setPortUnavailable(int port) const {
	m_server->setPortUnavailable(m_applicationId, port);
}

void This::Com::releasePort(int port) const {
	m_server->releasePort(m_applicationId, port);
}

std::unique_ptr<RequestSocket> This::Com::createRequestSocket(const std::string& endpoint, const std::string& responderIdentity) const {
	return m_server->createRequestSocket(endpoint, responderIdentity);
}

std::unique_ptr<RequestSocket> This::Com::createRequestSocket(const std::string& endpoint, const std::string& responderIdentity, int timeout) const {
	return m_server->createRequestSocket(endpoint, responderIdentity, timeout);
}

State This::parseState(const std::string& value) {

	if (value == "UNKNOWN") {
		return UNKNOWN;
	} else if (value == "STARTING") {
		return STARTING;
	} else if (value == "RUNNING") {
		return RUNNING;
	} else if (value == "STOPPING") {
		return STOPPING;
	} else if (value == "KILLING") {
		return KILLING;
	} else if (value == "PROCESSING_ERROR") {
		return PROCESSING_ERROR;
	} else if (value == "ERROR") {
		return FAILURE;
	} else if (value == "SUCCESS") {
		return SUCCESS;
	} else if (value == "STOPPED") {
		return STOPPED;
	} else if (value == "KILLED") {
		return KILLED;
	}

	return UNKNOWN;
}

void This::init(int argc, char *argv[]) {
	if (!m_instance.m_inited) {
		m_instance.initApplication(argc, argv);
	}
}

void This::init(const std::string& name, const std::string& endpoint) {
	if (!m_instance.m_inited) {
		m_instance.initApplication(name, endpoint);
	}
}

void This::terminate() {

	// Test if termination is already done.
	if (!m_instance.m_inited) {
		return;
	}

	// Terminate the unregistered application.
	if (!m_instance.m_registered) {
		m_instance.terminateUnregisteredApplication();
	}

	// Inited.
	m_instance.m_inited = false;
}


This::This() :
	//Services(),
	m_id(-1),
	m_registered(false),
	m_starterId(0),
	m_inited(false) {
}

void This::initApplication(int argc, char *argv[]) {

	if (argc == 0) {
		throw InvalidArgumentException("Missing info argument");
	}

	std::string info(argv[argc - 1]);

	// Get the info object.
	json::Object infoObject;
	if (!json::parse(infoObject, info)) {
		throw InvalidArgumentException("Bad format for info argument");
	}

	m_name = infoObject[message::ApplicationIdentity::NAME].GetString();
	m_serverEndpoint = Endpoint::parse(infoObject[message::ApplicationIdentity::SERVER].GetString());

	// Registered apps have the id key.
	if (infoObject.HasMember(message::ApplicationIdentity::ID)) {
		m_registered = true;
		m_id = infoObject[message::ApplicationIdentity::ID].GetInt();
	}

	// Get the starter info if it is present.
	if (infoObject.HasMember(message::ApplicationIdentity::STARTER)) {
		json::Value& starterValue = infoObject[message::ApplicationIdentity::STARTER];
		m_starterEndpoint = Endpoint::parse(starterValue[message::ApplicationIdentity::SERVER].GetString());
		m_starterName = starterValue[message::ApplicationIdentity::NAME].GetString();
		m_starterId = starterValue[message::ApplicationIdentity::ID].GetInt();
	}

	// Init the app.
	initApplication();
}

void This::initApplication(const std::string& name, const std::string& endpoint) {

	// Get the name.
	m_name = name;

	// Get the server endpoint.
	m_serverEndpoint = Endpoint::parse(endpoint);

	// The application is de-facto unregistered.
	m_registered = false;

	// Init the app.
	initApplication();
}

void This::initApplication() {

	// Create the local server.
	m_server = std::make_unique<Server>(m_serverEndpoint);

	// Registered apps have the id key.
	if (!m_registered) {
		int id = initUnregisteredApplication();
		if (id == -1) {
			throw UnregisteredApplicationException(std::string("Maximum number of applications ") + m_name + " reached");
		}
		m_id = id;
	}

	// Create the starter server.
	if (m_starterEndpoint.getAddress() != "") {
		m_starterServer = std::make_unique<Server>(m_starterEndpoint);
	}

	m_waitingSet = std::make_unique<WaitingSet>();

	// Init listener.
	setName(m_name);
	m_server->registerEventListener(this);

	// Init com.
	m_com = std::unique_ptr<Com>(new Com(m_server.get(), m_id));

	// Inited.
	m_inited = true;
}

This::~This() {
	// Do not delete the impl here because there will be order trouble.

	// Terminate the unregistered application.
	if (m_inited && !m_registered) {
		terminateUnregisteredApplication();
	}
}

const std::string& This::getName() {
	return m_instance.m_name;
}

int This::getId() {
	return m_instance.m_id;
}

void This::setTimeout(int value) {
	m_instance.m_server->setTimeout(value);
}

int This::getTimeout() {
	return m_instance.m_server->getTimeout();
}

const Endpoint& This::getEndpoint() {

	if (m_instance.m_inited) {
		return m_instance.m_serverEndpoint;
	}

	static Endpoint result;
	return result;
}

Server& This::getServer() {
	return *m_instance.m_server;
}

const This::Com& This::getCom() {
	return *m_instance.m_com;
}

Server& This::getStarterServer() {

	if (m_instance.m_starterServer.get() == nullptr) {
		throw StarterServerException();
	}

	return *m_instance.m_starterServer;
}

bool This::isAvailable(int timeout) {
	return m_instance.m_server->isAvailable(timeout);
}

bool This::isStopping() {
	return m_instance.getState(m_instance.m_id) == STOPPING;
}

void This::handleStop(StopFunctionType function, int stoppingTime) {
	m_instance.handleStopImpl(function, stoppingTime);
}

void This::cancelWaitings() {
	m_instance.m_waitingSet->cancelAll();
}

int This::initUnregisteredApplication() {
	json::Object response = m_server->requestJSON(createAttachUnregisteredRequest(m_name, GET_PROCESS_PID()));

	return response[message::RequestResponse::VALUE].GetInt();
}

void This::terminateUnregisteredApplication() {
	m_server->requestJSON(createDetachUnregisteredRequest(m_id));
}

bool This::setRunning() {
	json::Object response = m_instance.m_server->requestJSON(createSetStatusRequest(m_instance.m_id, RUNNING));

	int value = response[message::RequestResponse::VALUE].GetInt();
	if (value == -1) {
		return false;
	}

	return true;
}

void This::setBinaryResult(const std::string& data) {
	m_instance.m_server->requestJSON(createSetResultRequest(m_instance.m_id), data);
}

void This::setResult(const std::string& data) {

	std::string resultData;
	serialize(data, resultData);
	setBinaryResult(resultData);
}

State This::getState(int id) const {

	json::Object event = m_server->requestJSON(createGetStatusRequest(id));

	return event[message::StatusEvent::APPLICATION_STATE].GetInt();
}

State This::waitForStop() {

	// The function is executed in a thread in parallel.
	// Do not parallelize the calls to the request socket.
	State state = UNKNOWN;

	while (true) {
		// waits for a new incoming status
		std::unique_ptr<Event> event = popEvent();

		// The socket is canceled.
		if (event.get() == nullptr) {
			return UNKNOWN;
		}

		if (event->getId() == m_id) {
			StatusEvent * status = dynamic_cast<StatusEvent *>(event.get());

			if (status != nullptr) {
				state = status->getState();

				if (state == STOPPING
					|| state == KILLING) {
					return state;
				}
			}
		}
	}

	return UNKNOWN;
}

std::unique_ptr<Instance> This::connectToStarter() {

	if (m_instance.m_starterServer.get() == nullptr) {
		return std::unique_ptr<Instance>(nullptr);
	}

	// Iterate the instances to find the id
	InstanceArray instances = m_instance.m_starterServer->connectAll(m_instance.m_starterName);

	for (auto i = instances.begin(); i != instances.end(); ++i) {
		if ((*i)->getId() == m_instance.m_starterId) {
			return std::unique_ptr<Instance>(std::move(*i));
		}
	}

	return std::unique_ptr<Instance>(nullptr);
}

void This::stoppingFunction(StopFunctionType stop) {

	application::State state = waitForStop();

	// Only stop in case of STOPPING.
	if (state == application::STOPPING) {
		stop();
	}
}

void This::handleStopImpl(StopFunctionType function, int stoppingTime) {

	// Notify the server.
	m_server->requestJSON(createSetStopHandlerRequest(m_id, stoppingTime));

	// Create the handler.
	m_stopHandler = std::make_unique<HandlerImpl>(bind(&This::stoppingFunction, this, function));
}

///////////////////////////////////////////////////////////////////////////////
// Instance

Instance::Com::Com(Server * server) :
	m_server(server),
	m_applicationId(-1) {
}

std::string Instance::Com::getKeyValue(const std::string& key) const {
	// TODO catch exceptions and rethrow an exception: TerminatedException?
	return m_server->getKeyValue(m_applicationId, key);
}

Instance::Instance(Server * server) :
	m_server(server),
	m_id(-1),
	m_com(server),
	m_pastStates(0),
	m_initialState(UNKNOWN),
	m_lastState(UNKNOWN),
	m_hasResult(false),
	m_exitCode(-1) {
}

Instance::~Instance() {
	// Unregister the instance.
	m_server->unregisterEventListener(this);

	// The destructor has been added to avoid blocking ZeroMQ, because the inner objects destructors were not called.
}

void Instance::setId(int id) {
	m_id = id;
	m_com.m_applicationId = id;
}

const std::string& Instance::getName() const {
	return EventListener::m_name;
}

void Instance::setErrorMessage(const std::string& message) {
	m_errorMessage = message;
}

void Instance::setOutputStreamSocket(std::unique_ptr<OutputStreamSocket>& socket) {
	if (socket) {
		m_outputStreamSocket = std::move(socket);
		m_outputStreamSocket->setApplicationId(m_id);
	}
}

void Instance::setPastStates(State pastStates) {
	m_pastStates = pastStates;
}

void Instance::setInitialState(State state) {
	m_initialState = state;

	// It is important to set the last state, because in case of a call to the function now without any incoming state.
	m_lastState = state;
}

int Instance::getId() const {
	return m_id;
}

Endpoint Instance::getEndpoint() const {
	return m_server->getEndpoint();
}

Endpoint Instance::getStatusEndpoint() const {
	return m_server->getStatusEndpoint();
}

std::string Instance::getNameId() const {
	std::stringstream os;
	os << m_name << "." << m_id;

	return os.str();
}

const Instance::Com& Instance::getCom() const {
	return m_com;
}

bool Instance::hasResult() const {
	return m_hasResult;
}

bool Instance::exists() const {
	return (m_id != -1);
}

const std::string& Instance::getErrorMessage() const {
	return m_errorMessage;
}

bool Instance::stop() {
	try {
		Response response = m_server->stopApplicationAsynchronously(m_id, false);

	} catch (const ConnectionTimeout& e) {
		m_errorMessage = e.what();
		return false;
	}

	return true;
}

bool Instance::kill() {
	try {
		Response response = m_server->stopApplicationAsynchronously(m_id, true);

	} catch (const ConnectionTimeout& e) {
		m_errorMessage = e.what();
		return false;
	}

	return true;
}

State Instance::waitFor(int states, KeyValue& keyValue, bool blocking) {

	// Create a scoped waiting so that it is removed at the exit of the function.
	Waiting scopedWaiting(std::bind(&Instance::cancelWaitFor, this));

	if (!exists()) {
		// The application was not launched.
		return m_lastState;
	}

	// Test the terminal state.
	if (m_lastState == SUCCESS
		|| m_lastState == STOPPED
		|| m_lastState == KILLED
		|| m_lastState == FAILURE) {
		// The application is already terminated
		return m_lastState;
	}

	// Test the requested states.
	if ((states & m_pastStates) != 0) {
		// The state is already received.
		return m_lastState;
	}

	while (true) {
		// Waits for a new incoming status.
		std::unique_ptr<Event> event = popEvent(blocking);

		// The non-blocking call returns a null message.
		if (event.get() == nullptr) {
			return m_lastState;
		}

		if (event->getId() == m_id) {
			StatusEvent * status = dynamic_cast<StatusEvent *>(event.get());

			if (status != nullptr) {
				State state = status->getState();
				m_pastStates = status->getPastStates();
				m_lastState = state;

				// Assign the exit code.
				if (status->getExitCode() != -1) {
					m_exitCode = status->getExitCode();
				}

				// Test the terminal state.
				if (state == SUCCESS
					|| state == STOPPED
					|| state == KILLED
					|| state == FAILURE) {
					break;
				}

				// Test the requested states.
				if ((states & m_pastStates) != 0) {
					return m_lastState;
				}
			}
			else {

				if (ResultEvent * result = dynamic_cast<ResultEvent *>(event.get())) {
					m_hasResult = true;
					m_resultData = result->getData();
				}
				else if (KeyEvent * keyEvent = dynamic_cast<KeyEvent *>(event.get())) {
					if (keyEvent->getKey() == keyValue.getKey()) {
						// Set the status and value.
						if (keyEvent->getStatus() == KeyEvent::Status::STORED) {
							keyValue.setStatus(KeyValue::Status::STORED);
						}
						else {
							keyValue.setStatus(KeyValue::Status::REMOVED);
						}
						break;
					}
				}
				else if (dynamic_cast<CancelEvent *>(event.get())) {
					break;
				}
			}
		}
	}

	return m_lastState;
}

State Instance::waitFor(int states) {
	KeyValue keyValue("");
	return waitFor(states, keyValue, true);
}

State Instance::waitFor() {
	KeyValue keyValue("");
	return waitFor(0, keyValue, true);
}

State Instance::waitFor(KeyValue& keyValue) {
	return waitFor(0, keyValue, true);
}

void Instance::cancelWaitFor() {
	cancel(m_id);
}

State Instance::now() {

	// First implementation used getLastState().
	return getActualState();
}

State Instance::getLastState() {
	KeyValue keyValue("");
	return waitFor(0, keyValue, false);
}

State Instance::getActualState() const {
	return m_server->getActualState(m_id);
}

std::set<State> Instance::getPastStates() const {
	return m_server->getPastStates(m_id);
}

int Instance::getExitCode() const {
	return m_exitCode;
}

std::optional<std::string> Instance::getBinaryResult() {

	waitFor();

	if (m_hasResult) {
		return m_resultData;
	}

	return {};
}

std::optional<std::string> Instance::getResult() {
	return getBinaryResult();
}

std::unique_ptr<OutputStreamSocket> Instance::getOutputStreamSocket() {
	return std::move(m_outputStreamSocket);
}


///////////////////////////////////////////////////////////////////////////
// Configuration

Configuration::Configuration(const std::string& name, const std::string& description, bool singleInstance, bool restart, int startingTime, int stoppingTime) {

	m_name = name;
	m_description = description;
	m_singleInstance = singleInstance;
	m_restart = restart;
	m_startingTime = startingTime;
	m_stoppingTime = stoppingTime;
}

const std::string& Configuration::getName() const {
	return m_name;
}

const std::string& Configuration::getDescription() const {
	return m_description;
}

bool Configuration::hasSingleInstance() const {
	return m_singleInstance;
}

bool Configuration::canRestart() const {
	return m_restart;
}

int Configuration::getStartingTime() const {
	return m_startingTime;
}

int Configuration::getStoppingTime() const {
	return m_stoppingTime;
}

///////////////////////////////////////////////////////////////////////////
// Info

Info::Info(const std::string& name, int id, int pid, State applicationState, State pastApplicationStates, const std::string& args) :
	m_id(id),
	m_pid(pid),
	m_applicationState(applicationState),
	m_pastApplicationStates(pastApplicationStates),
	m_args(args),
	m_name(name) {
}

int Info::getId() const {
	return m_id;
}

State Info::getState() const {
	return m_applicationState;
}

State Info::getPastStates() const {
	return m_pastApplicationStates;
}

const std::string& Info::getArgs() const {
	return m_args;
}

const std::string& Info::getName() const {
	return m_name;
}

int Info::getPid() const {
	return m_pid;
}

///////////////////////////////////////////////////////////////////////////
// Port

Port::Port(int port, const std::string& status, const std::string& owner) :
	m_port(port),
	m_status(status),
	m_owner(owner) {
}

int Port::getPort() const {
	return m_port;
}

const std::string& Port::getStatus() const {
	return m_status;
}

const std::string& Port::getOwner() const {
	return m_owner;
}

std::string toString(cameo::application::State applicationStates) {

	std::vector<std::string> states;

	if ((applicationStates & STARTING) != 0) {
		states.push_back("STARTING");
	}

	if ((applicationStates & RUNNING) != 0) {
		states.push_back("RUNNING");
	}

	if ((applicationStates & STOPPING) != 0) {
		states.push_back("STOPPING");
	}

	if ((applicationStates & KILLING) != 0) {
		states.push_back("KILLING");
	}

	if ((applicationStates & PROCESSING_ERROR) != 0) {
		states.push_back("PROCESSING_ERROR");
	}

	if ((applicationStates & FAILURE) != 0) {
		states.push_back("ERROR");
	}

	if ((applicationStates & SUCCESS) != 0) {
		states.push_back("SUCCESS");
	}

	if ((applicationStates & STOPPED) != 0) {
		states.push_back("STOPPED");
	}

	if ((applicationStates & KILLED) != 0) {
		states.push_back("KILLED");
	}

	if (states.size() == 0) {
		return "UNKNOWN";
	}

	if (states.size() == 1) {
		return states.front();
	}

	std::ostringstream result;

	for (size_t i = 0; i < states.size() - 1; i++) {
		result << states[i] << ", ";
	}
	result << states.back();

	return result.str();
}

///////////////////////////////////////////////////////////////////////////////
// operator<<

std::ostream& operator<<(std::ostream& os, const cameo::application::This& application) {

	os << application.m_name << "." << application.m_id << "@" << application.m_serverEndpoint;

	return os;
}

std::ostream& operator<<(std::ostream& os, const application::Instance& instance) {

	os << instance.m_name << "." << instance.m_id << "@" << instance.m_server->getEndpoint();

	return os;
}

std::ostream& operator<<(std::ostream& os, const application::Configuration& info) {

	os << "[name=" << info.m_name
			<< ", description=" << info.m_description
			<< ", single instance=" << info.m_singleInstance
			<< ", restart=" << info.m_restart
			<< ", starting time=" << info.m_startingTime
			<< ", stopping time=" << info.m_stoppingTime << "]";

	return os;
}

std::ostream& operator<<(std::ostream& os, const application::Info& info) {

	os << "[name=" << info.m_name
			<< ", id=" << info.m_id
			<< ", state=" << info.m_applicationState
			<< ", args=" << info.m_args << "]";

	return os;
}

std::ostream& operator<<(std::ostream& os, const application::Port& port) {

	os << "[port=" << port.m_port
			<< ", status=" << port.m_status
			<< ", owner=" << port.m_owner << "]";

	return os;
}

}
}
