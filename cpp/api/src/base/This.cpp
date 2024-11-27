/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

#include "This.h"

#include "ImplFactory.h"
#include "JSON.h"
#include "Waiting.h"
#include "WaitingSet.h"
#include "RequestSocket.h"
#include "CancelEvent.h"
#include "StatusEvent.h"

namespace cameo {

This This::m_instance;
const std::string This::RUNNING_STATE = "RUNNING";

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
	json::Object response {json::toJSON(m_server->request(createAttachUnregisteredRequest(m_name, GET_PROCESS_PID())))};

	return response[message::RequestResponse::VALUE].GetInt();
}

void This::terminateUnregisteredApplication() {
	m_server->request(createDetachUnregisteredRequest(m_id));
}

bool This::setRunning() {
	json::Object response {json::toJSON(m_instance.m_server->request(createSetStatusRequest(m_instance.m_id, state::RUNNING)))};

	int value {response[message::RequestResponse::VALUE].GetInt()};
	if (value == -1) {
		return false;
	}

	return true;
}

void This::setResult(const std::string& data) {
	m_instance.m_server->request(createSetResultRequest(m_instance.m_id), data);
}

state::Value This::getState(int id) const {

	json::Object event {json::toJSON(m_server->request(createGetStatusRequest(id)))};

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
	requestSocket->request(request);
}

void This::startCheckStatesThread() {

	if (!m_checkStatesThread) {
		m_checkStatesThread = std::make_unique<std::thread>(std::bind(&This::checkStates, this));
	}
}

void This::initStopCheck(StopFunctionType function, int stoppingTime) {

	// Notify the server.
	m_server->request(createSetStopHandlerRequest(m_id, stoppingTime));

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


}