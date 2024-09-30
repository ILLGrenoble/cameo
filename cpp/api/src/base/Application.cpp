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
#include "StatusEvent.h"
#include "EventStreamSocket.h"
#include "impl/StreamSocketImpl.h"
#include "Strings.h"
#include "JSON.h"
#include "Server.h"
#include "Messages.h"
#include "Waiting.h"
#include "WaitingSet.h"
#include "RequestSocket.h"
#include "ContextZmq.h"
#include "ImplFactory.h"
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

This This::m_instance;
const std::string This::RUNNING_STATE = "RUNNING";

ServerAndApp::ServerAndApp() {
}

ServerAndApp::ServerAndApp(std::unique_ptr<Server>& server, std::unique_ptr<App>& app) :
	m_server(std::move(server)), m_app(std::move(app)) {
}

Server& ServerAndApp::getServer() {
	return *m_server.get();
}

bool ServerAndApp::hasApp() const {
	return m_app.get() != nullptr;
}

App& ServerAndApp::getApp() {
	return *m_app.get();
}

void ServerAndApp::terminate() {

	m_server->terminate();
	m_app->terminate();
}


This::Com::Com(Server * server, int applicationId) :
	m_server(server),
	m_applicationId(applicationId) {
}

Context* This::Com::getContext() const {
	return m_server->m_context.get();
}

int This::Com::getResponderProxyPort() const {
	return m_server->getResponderProxyPort();
}

int This::Com::getPublisherProxyPort() const {
	return m_server->getPublisherProxyPort();
}

int This::Com::getSubscriberProxyPort() const {
	return m_server->getSubscriberProxyPort();
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

state::Value This::parseState(const std::string& value) {

	if (value == "NIL") {
		return state::NIL;
	}
	else if (value == "STARTING") {
		return state::STARTING;
	}
	else if (value == "RUNNING") {
		return state::RUNNING;
	}
	else if (value == "STOPPING") {
		return state::STOPPING;
	}
	else if (value == "KILLING") {
		return state::KILLING;
	}
	else if (value == "PROCESSING_FAILURE") {
		return state::PROCESSING_FAILURE;
	}
	else if (value == "FAILURE") {
		return state::FAILURE;
	}
	else if (value == "SUCCESS") {
		return state::SUCCESS;
	}
	else if (value == "STOPPED") {
		return state::STOPPED;
	}
	else if (value == "KILLED") {
		return state::KILLED;
	}

	return state::NIL;
}

void This::init(int argc, char *argv[]) {
	if (!m_instance.m_inited) {
		m_instance.initApplication(argc, argv);
	}
}

void This::init(const std::string& name, const Endpoint& endpoint) {
	if (!m_instance.m_inited) {
		m_instance.initApplication(name, endpoint);
	}
}

void This::init(const std::string& name, const std::string& endpoint) {
	if (!m_instance.m_inited) {
		m_instance.initApplication(name, endpoint);
	}
}

void This::terminate() {
	m_instance.terminateImpl();
}

void This::terminateImpl() {

	// Test if termination is already done.
	if (!m_inited) {
		return;
	}

	// Terminate the unregistered application.
	if (!m_registered) {
		terminateUnregisteredApplication();
	}

	// Join the check states thread if it was started.
	if (m_checkStatesThread) {

		// Cancel the listener if it is necessary.
		EventListener::cancel(m_id);

		// Join the thread.
		m_checkStatesThread->join();
	}
	
	// Reset the starter server.
	m_starterServer.reset();

	// Reset the server.
	m_server.reset();
	
	// Terminate the default context to ensure good cleanup. 
	ImplFactory::terminateDefaultContext();

	// Inited.
	m_inited = false;
}


This::This() :
	m_id{-1},
	m_registered{false},
	m_starterId{0},
	m_starterProxyPort{0},
	m_starterLinked{false},
	m_inited{false} {
}

void This::initApplication(int argc, char *argv[]) {

	if (argc == 0) {
		throw InvalidArgumentException("Missing info argument");
	}

	std::string info {argv[argc - 1]};

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
		m_starterProxyPort = infoObject[message::ApplicationIdentity::STARTER_PROXY_PORT].GetInt();
		m_starterLinked = infoObject[message::ApplicationIdentity::STARTER_LINKED].GetBool();
	}

	// Init the app.
	initApplication();
}

void This::initApplication(const std::string& name, const Endpoint& endpoint) {

	// Get the server endpoint.
	m_serverEndpoint = endpoint;

	// Get the name.
	m_name = name;

	// The application is de-facto unregistered.
	m_registered = false;

	// Init the app.
	initApplication();
}

void This::initApplication(const std::string& name, const std::string& endpoint) {

	try {
		This::initApplication(name, Endpoint::parse(endpoint));
	}
	catch (...) {
		throw InvalidArgumentException(std::string{"Cannot initialize the app with server "} + endpoint + ": invalid endpoint");
	}
}

void This::initApplication() {

	// Create the local server.
	m_server = Server::create(m_serverEndpoint);
	m_server->init();

	// Registered apps have the id key.
	if (!m_registered) {
		int id {initUnregisteredApplication()};
		if (id == -1) {
			throw UnregisteredApplicationException(std::string("Maximum number of applications ") + m_name + " reached");
		}
		m_id = id;
	}

	m_waitingSet = std::make_unique<WaitingSet>();

	// Init listener.
	EventListener::setName(m_name);
	m_server->registerEventListener(this);

	// Init the check of the starter if it is linked.
	if (m_starterLinked) {
		initStarterCheck();
	}

	// Init com.
	m_com = std::unique_ptr<Com>{new Com(m_server.get(), m_id)};

	// Inited.
	m_inited = true;
}

This::~This() {
	terminate();
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

bool This::isAvailable(int timeout) {
	return m_instance.m_server->isAvailable(timeout);
}

bool This::isStopping() {
	return m_instance.getState(m_instance.m_id) == state::STOPPING;
}

void This::handleStop(StopFunctionType function, int stoppingTime) {
	m_instance.initStopCheck(function, stoppingTime);
}

void This::cancelAll() {
	m_instance.m_waitingSet->cancelAll();
}

int This::initUnregisteredApplication() {
	json::Object response {m_server->requestJSON(createAttachUnregisteredRequest(m_name, GET_PROCESS_PID()))};

	return response[message::RequestResponse::VALUE].GetInt();
}

void This::terminateUnregisteredApplication() {
	m_server->requestJSON(createDetachUnregisteredRequest(m_id));
}

bool This::setRunning() {
	json::Object response {m_instance.m_server->requestJSON(createSetStatusRequest(m_instance.m_id, state::RUNNING))};

	int value {response[message::RequestResponse::VALUE].GetInt()};
	if (value == -1) {
		return false;
	}

	return true;
}

void This::setResult(const std::string& data) {
	m_instance.m_server->requestJSON(createSetResultRequest(m_instance.m_id), data);
}

state::Value This::getState(int id) const {

	json::Object event {m_server->requestJSON(createGetStatusRequest(id))};

	return event[message::StatusEvent::APPLICATION_STATE].GetInt();
}

std::unique_ptr<ServerAndApp> This::connectToStarter(int options, int timeout) {

	// Create the starter server.
	if (m_instance.m_starterEndpoint.getAddress() == "") {
		return {};
	}

	std::unique_ptr<Server> server;
	std::unique_ptr<App> app;

	// Create the server with proxy or not.
	bool useProxy = ((options & option::USE_PROXY) != 0);
	if (useProxy) {
		server = Server::create(m_instance.m_starterEndpoint.withPort(m_instance.m_starterProxyPort), option::USE_PROXY);
	}
	else {
		server = Server::create(m_instance.m_starterEndpoint);
	}

	// Set the server init timeout.
	server->setTimeout(timeout);

	try {
		server->init();

		// Iterate the instances to find the id
		AppArray instances = server->connectAll(m_instance.m_starterName, options);

		for (auto i = instances.begin(); i != instances.end(); ++i) {
			if ((*i)->getId() == m_instance.m_starterId) {
				app = std::unique_ptr<App>(std::move(*i));
				break;
			}
		}
	}
	catch (...) {
		// Timeout while initializing the server.
	}

	return std::unique_ptr<ServerAndApp>(new ServerAndApp(server, app));
}

void This::stop() {

	// Use a request socket to avoid any race condition.
	std::unique_ptr<RequestSocket> requestSocket{m_server->createServerRequestSocket()};

	std::string request {createStopRequest(m_id, true)};
	requestSocket->requestJSON(request);
}

void This::startCheckStatesThread() {

	if (!m_checkStatesThread) {
		m_checkStatesThread = std::make_unique<std::thread>(std::bind(&This::checkStates, this));
	}
}

void This::initStopCheck(StopFunctionType function, int stoppingTime) {

	// Notify the server.
	m_server->requestJSON(createSetStopHandlerRequest(m_id, stoppingTime));

	// Memorize the stop function.
	m_stopFunction = function;

	// Start the check states thread.
	startCheckStatesThread();
}

void This::initStarterCheck() {

	// Create the starter server.
	// If the starter has a running proxy, then use the proxy is reasonable.
	if (m_starterProxyPort != 0) {
		m_starterServer = Server::create(m_starterEndpoint.withPort(m_instance.m_starterProxyPort), option::USE_PROXY);
	}
	else {
		m_starterServer = Server::create(m_starterEndpoint);
	}

	m_starterServer->init();

	// Register this as event listener.
	m_starterServer->registerEventListener(this, false);

	// Get the actual state. It is necessary to get the actual state after the registration so that we do not miss any events.
	state::Value state {m_starterServer->getState(m_starterId)};

	// Stop this app if the starter is already terminated i.e. the state is NIL.
	if (state == state::NIL) {
		stop();
	}
	else {
		startCheckStatesThread();
	}
}

void This::checkStates() {

	// The function is executed in a thread in parallel.
	// Do not parallelize the calls to the request socket.
	while (true) {
		// Wait for a new incoming status.
		std::unique_ptr<Event> event {EventListener::popEvent(true)};

		// Test if the socket is canceled.
		if (event.get() == nullptr || dynamic_cast<CancelEvent *>(event.get()) != nullptr) {
			break;
		}

		// Filter events coming from this.
		if (event->getId() == m_id) {
			StatusEvent * status {dynamic_cast<StatusEvent *>(event.get())};

			if (status != nullptr) {
				state::Value state {status->getState()};

				// Call the stop function if stop has been requested.
				if (state == state::STOPPING) {

					if (m_stopFunction) {
						m_stopFunction();
					}
					break;
				}
			}
		}

		// Filter events coming from starter.
		if (event->getId() == m_starterId) {
			StatusEvent * status {dynamic_cast<StatusEvent *>(event.get())};

			if (status != nullptr) {
				state::Value state {status->getState()};

				// Stop this application if it was linked.
				if (state == state::STOPPED || state == state::KILLED || state == state::SUCCESS || state == state::FAILURE) {
					stop();
				}
			}
		}
	}

	// Reset the stop function here because in case of Python callback, it is necessary to do it here rather than in the This destructor.
	m_stopFunction = StopFunctionType{};
}

std::string This::toString() {
	return AppIdentity{m_instance.m_name, m_instance.m_id, ServerIdentity{m_instance.m_server->getEndpoint().toString(), m_instance.m_server->usesProxy()}}.toJSONString();
}

///////////////////////////////////////////////////////////////////////////////
// Instance

App::Com::Com(Server * server) :
	m_server{server},
	m_applicationId{-1} {
}

int App::Com::getResponderProxyPort() const {
	return m_server->getResponderProxyPort();
}

int App::Com::getPublisherProxyPort() const {
	return m_server->getPublisherProxyPort();
}

int App::Com::getSubscriberProxyPort() const {
	return m_server->getSubscriberProxyPort();
}

std::string App::Com::getKeyValue(const std::string& key) const {
	// TODO catch exceptions and rethrow an exception: TerminatedException?
	return m_server->getKeyValue(m_applicationId, key);
}

App::Com::KeyValueGetterException::KeyValueGetterException(const std::string& message) :
	RemoteException(message) {
}

App::Com::KeyValueGetter::KeyValueGetter(Server* server, const std::string& name, int id, const std::string& key) :
	m_server{server},
	m_id{id},
	m_key{key},
	m_canceled{false} {

	EventListener::setName(name);

	m_server->registerEventListener(this);
}

App::Com::KeyValueGetter::~KeyValueGetter() {

	m_server->unregisterEventListener(this);
}

std::string App::Com::KeyValueGetter::get(const TimeoutCounter& timeoutCounter) {

	// Create a scoped waiting so that it is removed at the exit of the function.
	Waiting scopedWaiting {std::bind(&App::Com::KeyValueGetter::cancel, this)};

	try {
		return m_server->getKeyValue(m_id, m_key);
	}
	catch (const UndefinedApplicationException& e) {
		// The application has already terminated.
		throw KeyValueGetterException("Application terminated");
	}
	catch (const UndefinedKeyException& e) {
		// Key is not found, waiting for the event.
	}

	while (true) {
		// Waits for a new incoming status. The call may throw a a timeout.
		int remainingTimeout = timeoutCounter.remains();

		std::unique_ptr<Event> event {EventListener::popEvent(true, remainingTimeout)};

		if (!event) {
			return "";
		}

		if (event->getId() == m_id) {
			StatusEvent * status {dynamic_cast<StatusEvent *>(event.get())};

			if (status != nullptr) {
				state::Value state {status->getState()};

				// Test the terminal state.
				if (state == state::SUCCESS
					|| state == state::STOPPED
					|| state == state::KILLED
					|| state == state::FAILURE) {
					throw KeyValueGetterException("Application terminated");
				}
			}
			else {
				KeyEvent * keyEvent {dynamic_cast<KeyEvent *>(event.get())};
				if (keyEvent) {
					if (keyEvent->getKey() == m_key) {
						// Set the status and value.
						if (keyEvent->getStatus() == KeyEvent::Status::STORED) {
							return keyEvent->getValue();
						}

						throw KeyValueGetterException("Key removed");
					}
				}
				else if (dynamic_cast<CancelEvent *>(event.get())) {
					return "";
				}
			}
		}
	}
}

void App::Com::KeyValueGetter::cancel() {
	m_canceled = true;
	EventListener::cancel(m_id);
}

bool App::Com::KeyValueGetter::isCanceled() const {
	return m_canceled;
}

std::unique_ptr<App::Com::KeyValueGetter> App::Com::createKeyValueGetter(const std::string& key) const {
	return std::unique_ptr<App::Com::KeyValueGetter>{new App::Com::KeyValueGetter(m_server, m_name, m_applicationId, key)};
}

App::App(Server * server) :
	m_server{server},
	m_id{-1},
	m_com{server},
	m_pastStates{0},
	m_initialState{state::NIL},
	m_lastState{state::NIL},
	m_hasResult{false},
	m_exitCode{-1} {
}

App::~App() {
	terminate();

	// The destructor has been added to avoid blocking ZeroMQ, because the inner objects destructors were not called.
}

void App::terminate() {
	m_outputStreamSocket.reset();

	// Unregister the instance.
	m_server->unregisterEventListener(this);
}

void App::setId(int id) {
	m_id = id;
	m_com.m_applicationId = id;
	m_com.m_name = getName();
}

const std::string& App::getName() const {
	return EventListener::m_name;
}

void App::setOutputStreamSocket(std::unique_ptr<OutputStreamSocket>& socket) {
	if (socket) {
		m_outputStreamSocket = std::move(socket);
		m_outputStreamSocket->setApplicationId(m_id);
	}
}

void App::setPastStates(state::Value pastStates) {
	m_pastStates = pastStates;
}

void App::setInitialState(state::Value state) {
	m_initialState = state;

	// It is important to set the last state, because in case of a call to the function now without any incoming state.
	m_lastState = state;
}

int App::getId() const {
	return m_id;
}

bool App::usesProxy() const {
	return m_server->usesProxy();
}

Endpoint App::getEndpoint() const {
	return m_server->getEndpoint();
}

Endpoint App::getStatusEndpoint() const {
	return m_server->getStatusEndpoint();
}

std::string App::getNameId() const {
	return m_name + "." + std::to_string(m_id);
}

const App::Com& App::getCom() const {
	return m_com;
}

bool App::hasResult() const {
	return m_hasResult;
}

std::unique_ptr<App> App::connect(int options) const {
	return m_server->connect(m_id, options);
}

bool App::stop() {
	try {
		Response response {m_server->stop(m_id, false)};
	}
	catch (const ConnectionTimeout& e) {
		return false;
	}

	return true;
}

bool App::kill() {
	try {
		Response response {m_server->stop(m_id, true)};
	}
	catch (const ConnectionTimeout& e) {
		return false;
	}

	return true;
}

state::Value App::waitFor(int states, KeyValue& keyValue, bool blocking) {

	// Create a scoped waiting so that it is removed at the exit of the function.
	Waiting scopedWaiting {std::bind(&App::cancel, this)};

	// Test the terminal state.
	if (m_lastState == state::SUCCESS
		|| m_lastState == state::STOPPED
		|| m_lastState == state::KILLED
		|| m_lastState == state::FAILURE) {
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
		std::unique_ptr<Event> event {popEvent(blocking)};

		// The non-blocking call returns a null message.
		if (event.get() == nullptr) {
			return m_lastState;
		}

		if (event->getId() == m_id) {
			StatusEvent * status {dynamic_cast<StatusEvent *>(event.get())};

			if (status != nullptr) {
				state::Value state {status->getState()};
				m_pastStates = status->getPastStates();
				m_lastState = state;

				// Assign the exit code.
				if (status->getExitCode() != -1) {
					m_exitCode = status->getExitCode();
				}

				// Test the terminal state.
				if (state == state::SUCCESS
					|| state == state::STOPPED
					|| state == state::KILLED
					|| state == state::FAILURE) {
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

state::Value App::waitFor(int states) {
	KeyValue keyValue {""};
	return waitFor(states, keyValue, true);
}

state::Value App::waitFor() {
	KeyValue keyValue {""};
	return waitFor(0, keyValue, true);
}

state::Value App::waitFor(KeyValue& keyValue) {
	return waitFor(0, keyValue, true);
}

void App::cancel() {
	EventListener::cancel(m_id);
}

state::Value App::getLastState() {
	KeyValue keyValue {""};
	return waitFor(0, keyValue, false);
}

state::Value App::getActualState() const {
	return getState();
}

state::Value App::getState() const {
	return m_server->getState(m_id);
}

std::set<state::Value> App::getPastStates() const {
	return m_server->getPastStates(m_id);
}

int App::getExitCode() const {
	return m_exitCode;
}

state::Value App::getInitialState() const {
	return m_initialState;
}

std::optional<std::string> App::getResult() {

	waitFor();

	if (m_hasResult) {
		return m_resultData;
	}

	return {};
}

std::unique_ptr<OutputStreamSocket> App::getOutputStreamSocket() {
	return std::move(m_outputStreamSocket);
}

std::string App::toString() const {
	return AppIdentity{m_name, m_id, ServerIdentity{m_server->getEndpoint().toString(), m_server->usesProxy()}}.toJSONString();
}

///////////////////////////////////////////////////////////////////////////
// Configuration

App::Config::Config(const std::string& name, const std::string& description, int multiple, bool restart, int startingTime, int stoppingTime) :
	m_name{name},
	m_description{description},
	m_multiple{multiple},
	m_restart{restart},
	m_startingTime{startingTime},
	m_stoppingTime{stoppingTime} {
}

const std::string& App::Config::getName() const {
	return m_name;
}

const std::string& App::Config::getDescription() const {
	return m_description;
}

int App::Config::getMultiple() const {
	return m_multiple;
}

bool App::Config::canRestart() const {
	return m_restart;
}

int App::Config::getStartingTime() const {
	return m_startingTime;
}

int App::Config::getStoppingTime() const {
	return m_stoppingTime;
}

std::string App::Config::toString() const {

	json::StringObject jsonObject;

	jsonObject.pushKey("name");
	jsonObject.pushValue(m_name);

	jsonObject.pushKey("description");
	jsonObject.pushValue(m_description);

	jsonObject.pushKey("multiple");
	jsonObject.pushValue(m_multiple);

	jsonObject.pushKey("restart");
	jsonObject.pushValue(m_restart);

	jsonObject.pushKey("starting_time");
	jsonObject.pushValue(m_startingTime);

	jsonObject.pushKey("stopping_time");
	jsonObject.pushValue(m_stoppingTime);

	return jsonObject.dump();
}

///////////////////////////////////////////////////////////////////////////
// Info

App::Info::Info(const std::string& name, int id, int pid, state::Value applicationState, state::Value pastApplicationStates, const std::string& args) :
	m_id{id},
	m_pid{pid},
	m_applicationState{applicationState},
	m_pastApplicationStates{pastApplicationStates},
	m_args{args},
	m_name{name} {
}

int App::Info::getId() const {
	return m_id;
}

state::Value App::Info::getState() const {
	return m_applicationState;
}

state::Value App::Info::getPastStates() const {
	return m_pastApplicationStates;
}

const std::string& App::Info::getArgs() const {
	return m_args;
}

const std::string& App::Info::getName() const {
	return m_name;
}

int App::Info::getPid() const {
	return m_pid;
}

std::string App::Info::toString() const {

	json::StringObject jsonObject;

	jsonObject.pushKey("name");
	jsonObject.pushValue(m_name);

	jsonObject.pushKey("id");
	jsonObject.pushValue(m_id);

	jsonObject.pushKey("state");
	jsonObject.pushValue(cameo::toString(m_applicationState));

	jsonObject.pushKey("past_states");
	jsonObject.pushValue(cameo::toString(m_pastApplicationStates));

	jsonObject.pushKey("args");
	jsonObject.pushValue(m_args);

	return jsonObject.dump();
}

///////////////////////////////////////////////////////////////////////////
// Port

App::Port::Port(int port, const std::string& status, const std::string& owner) :
	m_port{port},
	m_status{status},
	m_owner{owner} {
}

int App::Port::getPort() const {
	return m_port;
}

const std::string& App::Port::getStatus() const {
	return m_status;
}

const std::string& App::Port::getOwner() const {
	return m_owner;
}

std::string App::Port::toString() const {

	json::StringObject jsonObject;

	jsonObject.pushKey("port");
	jsonObject.pushValue(m_port);

	jsonObject.pushKey("status");
	jsonObject.pushValue(m_status);

	jsonObject.pushKey("owner");
	jsonObject.pushValue(m_owner);

	return jsonObject.dump();
}

std::string toString(cameo::state::Value applicationStates) {

	std::vector<std::string> states;

	if ((applicationStates & state::STARTING) != 0) {
		states.push_back("STARTING");
	}

	if ((applicationStates & state::RUNNING) != 0) {
		states.push_back("RUNNING");
	}

	if ((applicationStates & state::STOPPING) != 0) {
		states.push_back("STOPPING");
	}

	if ((applicationStates & state::KILLING) != 0) {
		states.push_back("KILLING");
	}

	if ((applicationStates & state::PROCESSING_FAILURE) != 0) {
		states.push_back("PROCESSING_FAILURE");
	}

	if ((applicationStates & state::FAILURE) != 0) {
		states.push_back("FAILURE");
	}

	if ((applicationStates & state::SUCCESS) != 0) {
		states.push_back("SUCCESS");
	}

	if ((applicationStates & state::STOPPED) != 0) {
		states.push_back("STOPPED");
	}

	if ((applicationStates & state::KILLED) != 0) {
		states.push_back("KILLED");
	}

	if (states.size() == 0) {
		return "NIL";
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

std::ostream& operator<<(std::ostream& os, const App& instance) {

	os << instance.toString();

	return os;
}

std::ostream& operator<<(std::ostream& os, const App::Config& configuration) {

	os << configuration.toString();

	return os;
}

std::ostream& operator<<(std::ostream& os, const App::Info& info) {

	os << info.toString();

	return os;
}

std::ostream& operator<<(std::ostream& os, const App::Port& port) {

	os << port.toString();

	return os;
}

}
