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

#include <sstream>
#include <iostream>
#include <iostream>
#include <stdexcept>
#include <vector>

#include "JSON.h"
#include "EventStreamSocket.h"
#include "impl/ServicesImpl.h"
#include "impl/PublisherImpl.h"
#include "impl/RequesterImpl.h"
#include "impl/RequestImpl.h"
#include "impl/ResponderImpl.h"
#include "impl/SubscriberImpl.h"
#include "impl/WaitingImpl.h"
#include "impl/WaitingImplSet.h"
#include "impl/HandlerImpl.h"
#include "impl/StreamSocketImpl.h"
#include "impl/RequestSocketImpl.h"
#include "message/Message.h"
#include "Server.h"
#include "StarterServerException.h"
#include "StatusEvent.h"
#include "PublisherEvent.h"
#include "ResultEvent.h"
#include "PortEvent.h"
#include "KeyEvent.h"
#include "CancelEvent.h"

using namespace std;

namespace cameo {
namespace application {

This This::m_instance;
const std::string This::RUNNING_STATE = "RUNNING";

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
	if (m_instance.m_impl == nullptr) {
		m_instance.initApplication(argc, argv);
	}
}

std::string This::getReference() {
	if (m_instance.m_impl != nullptr) {
		ostringstream os;
		os << getName() << "." << getId() << "@" << getEndpoint();
		return os.str();
	}
	return "";
}

void This::terminate() {

	// Test if termination is already done.
	if (m_instance.m_impl == nullptr) {
		return;
	}

	// Terminate the unmanaged application.
	if (!m_instance.m_managed) {
		m_instance.terminateUnmanagedApplication();
	}

	// Terminate the services.
	m_instance.Services::terminate();

	// Ensure that it won't be done twice.
	m_instance.m_impl = nullptr;
}


This::This() :
	Services(),
	m_id(-1),
	m_managed(false),
	m_starterId(0) {
}

void This::initApplication(int argc, char *argv[]) {

	Services::init();

	if (argc == 0) {
		throw InvalidArgumentException("missing info argument");
	}

	string info(argv[argc - 1]);
	vector<string> tokens = split(info);

	if (tokens.size() < 4) {
		throw InvalidArgumentException(info + " is not a valid argument");
	}

	m_url = tokens[0] + ":" + tokens[1];

	string port = tokens[2];
	istringstream is(port);
	is >> m_port;

	// We separated host endpoint and server in the past (server being tcp://localhost)
	// but that generates troubles when two applications communicate remotely.
	// However leave the same value seems to be ok.
	m_serverEndpoint = m_url + ":" + port;

	// Create the request socket. The server endpoint has been defined.
	Services::initRequestSocket();

	// Retrieve the server version.
	Services::retrieveServerVersion();

	string nameId = tokens[3];

	int index = nameId.find_last_of('.');

	// Search for the . character meaning that the application is managed and already has an id.
	if (index != string::npos) {
		m_managed = true;
		m_name = nameId.substr(0, index);
		string sid = nameId.substr(index + 1);
		{
			istringstream is(sid);
			is >> m_id;
		}
	}
	else {
		m_managed = false;
		m_name = nameId;
		m_id = initUnmanagedApplication();

		if (m_id == -1) {
			throw UnmanagedApplicationException(string("Maximum number of applications ") + m_name + " reached");
		}
	}

	if (tokens.size() >= 7) {
		index = tokens[4].find_last_of('@');
		m_starterEndpoint = tokens[4].substr(index + 1) + ":" + tokens[5] + ":" + tokens[6];
		string starterNameId = tokens[4].substr(0, index);
		index = starterNameId.find_last_of('.');
		m_starterName = starterNameId.substr(0, index);
		string sid = starterNameId.substr(index + 1);
		{
			istringstream is(sid);
			is >> m_starterId;
		}
	}

	// Must be here because the server endpoint is required.
	initStatus();

	// Create the local server
	m_server = unique_ptr<Server>(new Server(m_serverEndpoint));

	// Create the starter server
	if (m_starterEndpoint != "") {
		m_starterServer = unique_ptr<Server>(new Server(m_starterEndpoint));
	}

	m_waitingSet = unique_ptr<WaitingImplSet>(new WaitingImplSet());

	// Init listener.
	setName(m_name);
	m_server->registerEventListener(this);
}

This::~This() {
	// Do not delete the impl here because there will be order trouble.

	// Terminate the unmanaged application.
	if (m_impl != nullptr && !m_managed) {
		terminateUnmanagedApplication();
	}
}

const std::string& This::getName() {
	return m_instance.m_name;
}

int This::getId() {
	return m_instance.m_id;
}

void This::setTimeout(int timeout) {
	m_instance.Services::setTimeout(timeout);
}

int This::getTimeout() {
	return m_instance.Services::getTimeout();
}

const std::string& This::getEndpoint() {
	if (m_instance.m_impl != nullptr) {
		return m_instance.m_serverEndpoint;
	}
	static string result;
	return result;
}

Server& This::getServer() {
	return *m_instance.m_server;
}

Server& This::getStarterServer() {

	if (m_instance.m_starterServer.get() == nullptr) {
		throw StarterServerException();
	}

	return *m_instance.m_starterServer;
}

const std::string& This::getUrl() {
	return m_instance.Services::getUrl();
}

bool This::isAvailable(int timeout) {
	return m_instance.Services::isAvailable(timeout);
}

bool This::isStopping() {
	return m_instance.getState(m_instance.m_id) == STOPPING;
}

void This::cancelWaitings() {
	m_instance.m_waitingSet->cancelAll();
}

int This::initUnmanagedApplication() {

	unique_ptr<zmq::message_t> reply = m_requestSocket->request(m_impl->createAttachUnmanagedRequest(m_name));

	// Get the JSON response.
	json::Object response;
	json::parse(response, reply.get());

	return response[message::RequestResponse::VALUE].GetInt();
}

void This::terminateUnmanagedApplication() {

	m_requestSocket->request(m_impl->createDetachUnmanagedRequest(m_id));
}

bool This::setRunning() {

	unique_ptr<zmq::message_t> reply = m_instance.m_requestSocket->request(m_instance.m_impl->createSetStatusRequest(m_instance.m_id, RUNNING));

	// Get the JSON response.
	json::Object response;
	json::parse(response, reply.get());

	int value = response[message::RequestResponse::VALUE].GetInt();
	if (value == -1) {
		return false;
	}

	return true;
}

void This::setBinaryResult(const std::string& data) {

	m_instance.m_requestSocket->request(m_instance.m_impl->createSetResultRequest(m_instance.m_id), data);
}

void This::setResult(const std::string& data) {

	string resultData;
	serialize(data, resultData);
	setBinaryResult(resultData);
}

State This::getState(int id) const {

	unique_ptr<zmq::message_t> reply = m_requestSocket->request(m_impl->createGetStatusRequest(id));

	// Get the JSON response.
	json::Object event;
	json::parse(event, reply.get());

	return event[message::StatusEvent::APPLICATION_STATE].GetInt();
}

bool This::destroyPublisher(const std::string& name) const {

	unique_ptr<zmq::message_t> reply = m_requestSocket->request(m_impl->createTerminatePublisherRequest(m_id, name));

	// Get the JSON response.
	json::Object response;
	json::parse(response, reply.get());

	int value = response[message::RequestResponse::VALUE].GetInt();

	return (value != -1);
}

bool This::removePort(const std::string& name) const {

	unique_ptr<zmq::message_t> reply = m_requestSocket->request(m_impl->createRemovePortV0Request(m_id, name));

	// Get the JSON response.
	json::Object response;
	json::parse(response, reply.get());

	int value = response[message::RequestResponse::VALUE].GetInt();

	return (value != -1);
}

State This::waitForStop() {

	// test if stop was requested elsewhere
	State state = getState(m_id);
	if (state == STOPPING
		|| state == KILLING) {
		return state;
	}

	while (true) {
		// waits for a new incoming status
		unique_ptr<Event> event = popEvent();

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
		return unique_ptr<Instance>(nullptr);
	}

	// Iterate the instances to find the id
	InstanceArray instances = m_instance.m_starterServer->connectAll(m_instance.m_starterName);

	for (int i = 0; i < instances.size(); i++) {
		if (instances[i]->getId() == m_instance.m_starterId) {
			return unique_ptr<Instance>(std::move(instances[i]));
		}
	}

	return unique_ptr<Instance>(nullptr);
}

void This::storeKeyValue(const std::string& key, const std::string& value) {
	m_instance.m_server->storeKeyValue(m_instance.m_id, key, value);
}

std::string This::getKeyValue(const std::string& key) {
	return m_instance.m_server->getKeyValue(m_instance.m_id, key);
}

void This::removeKey(const std::string& key) {
	m_instance.m_server->removeKey(m_instance.m_id, key);
}

void This::stoppingFunction(StopFunctionType stop) {

	application::State state = waitForStop();

	// Only stop in case of STOPPING.
	if (state == application::STOPPING) {
		stop();
	}
}

void This::handleStopImpl(StopFunctionType function) {
	m_stopHandler = unique_ptr<HandlerImpl>(new HandlerImpl(bind(&This::stoppingFunction, this, function)));
}

///////////////////////////////////////////////////////////////////////////////
// Instance

Instance::Instance(Server * server) :
	m_server(server),
	m_id(-1),
	m_pastStates(0),
	m_initialState(UNKNOWN),
	m_lastState(UNKNOWN),
	m_hasResult(false),
	m_exitCode(-1) {

	m_waiting.reset(new GenericWaitingImpl(bind(&Instance::cancelWaitFor, this)));
}

Instance::~Instance() {
	// Unregister the instance.
	m_server->unregisterEventListener(this);

	// The destructor has been added to avoid blocking ZeroMQ, because the inner objects destructors were not called.
}

void Instance::setId(int id) {
	m_id = id;
}

const std::string& Instance::getName() const {
	return EventListener::m_name;
}

void Instance::setErrorMessage(const std::string& message) {
	m_errorMessage = message;
}

void Instance::setOutputStreamSocket(std::unique_ptr<OutputStreamSocket>& socket) {
	m_outputStreamSocket = std::move(socket);
	m_outputStreamSocket->setApplicationId(m_id);
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

const std::string& Instance::getUrl() const {
	return m_server->getUrl();
}

const std::string& Instance::getEndpoint() const {
	return m_server->getEndpoint();
}

std::string Instance::getNameId() const {
	stringstream os;
	os << m_name << "." << m_id;

	return os.str();
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

State Instance::waitFor(int states, const std::string& eventName, KeyValue& keyValue, StateHandlerType handler, bool blocking) {

	if (!exists()) {
		// The application was not launched
		return m_lastState;
	}

	// Test the terminal state
	if (m_lastState == SUCCESS
		|| m_lastState == STOPPED
		|| m_lastState == KILLED
		|| m_lastState == FAILURE) {
		// The application is already terminated
		return m_lastState;
	}

	// Test the requested states
	if ((states & m_pastStates) != 0) {
		// The state is already received
		return m_lastState;
	}

	while (true) {
		// Waits for a new incoming status
		unique_ptr<Event> event = popEvent(blocking);

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

				// Call the state handler.
				if (handler != nullptr) {
					handler(state);
				}

				// test the terminal state
				if (state == SUCCESS
					|| state == STOPPED
					|| state == KILLED
					|| state == FAILURE) {
					break;
				}

				// test the requested states
				if ((states & m_pastStates) != 0) {
					return m_lastState;
				}
			}
			else {

				if (ResultEvent * result = dynamic_cast<ResultEvent *>(event.get())) {
					m_hasResult = true;
					m_resultData = result->getData();
				}
				else if (PublisherEvent * publisher = dynamic_cast<PublisherEvent *>(event.get())) {
					if (publisher->getPublisherName() == eventName) {
						break;
					}
				}
				else if (PortEvent * port = dynamic_cast<PortEvent *>(event.get())) {
					if (port->getPortName() == eventName) {
						break;
					}
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
						keyValue.setValue(keyEvent->getValue());
						break;
					}
				}
				else if (CancelEvent * cancel = dynamic_cast<CancelEvent *>(event.get())) {
					break;
				}
			}
		}
	}

	return m_lastState;
}

State Instance::waitFor(int states, StateHandlerType handler) {
	KeyValue keyValue("");
	return waitFor(states, "", keyValue, handler, true);
}

State Instance::waitFor(StateHandlerType handler) {
	KeyValue keyValue("");
	return waitFor(0, "", keyValue, handler, true);
}

State Instance::waitFor(const std::string& eventName) {
	KeyValue keyValue("");
	return waitFor(0, eventName, keyValue, nullptr, true);
}

State Instance::waitFor(KeyValue& keyValue) {
	return waitFor(0, "", keyValue, nullptr, true);
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
	return waitFor(0, "", keyValue, nullptr, false);
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

bool Instance::getBinaryResult(std::string& result) {

	waitFor();
	result = m_resultData;

	return m_hasResult;
}

bool Instance::getResult(std::string& result) {

	string bytes;
	getBinaryResult(bytes);
	parse(bytes, result);

	return m_hasResult;
}

std::shared_ptr<OutputStreamSocket> Instance::getOutputStreamSocket() {
	return m_outputStreamSocket;
}

std::string Instance::getKeyValue(const std::string& key) {
	// TODO catch exceptions and rethrow an exception: TerminatedException?
	return m_server->getKeyValue(m_id, key);
}

///////////////////////////////////////////////////////////////////////////
// InstanceArray

InstanceArray::InstanceArray() :
	m_size(0),
	m_array(0) {
}

InstanceArray::InstanceArray(const InstanceArray& array) :
	m_size(array.m_size),
	m_array(new unique_ptr<Instance>[m_size]) {

	// transferring pointers
	for (size_t i = 0; i < m_size; i++) {
		m_array[i] = std::move(array.m_array[i]);
	}
}

InstanceArray::~InstanceArray() {
	delete [] m_array;
}

void InstanceArray::allocate(std::size_t size) {
	m_size = size;
	m_array = new unique_ptr<Instance>[size];
}

std::size_t InstanceArray::size() const {
	return m_size;
}

std::unique_ptr<Instance>& InstanceArray::operator[](std::size_t index) {
	return m_array[index];
}

///////////////////////////////////////////////////////////////////////////////
// Publisher

Publisher::Publisher(This * application, int publisherPort, int synchronizerPort, const std::string& name, int numberOfSubscribers) :
	m_impl(new PublisherImpl(application, publisherPort, synchronizerPort, name, numberOfSubscribers)) {

	// Create the waiting here.
	m_waiting.reset(m_impl->waiting());
}

Publisher::~Publisher() {
}

std::unique_ptr<Publisher> Publisher::create(const std::string& name, int numberOfSubscribers) {

	unique_ptr<zmq::message_t> reply = This::m_instance.m_requestSocket->request(This::m_instance.m_impl->createCreatePublisherRequest(This::m_instance.m_id, name, numberOfSubscribers));

	// Get the JSON response.
	json::Object response;
	json::parse(response, reply.get());

	int publisherPort = response[message::PublisherResponse::PUBLISHER_PORT].GetInt();
	if (publisherPort == -1) {
		throw PublisherCreationException(response[message::PublisherResponse::MESSAGE].GetString());
	}
	int synchronizerPort = response[message::PublisherResponse::SYNCHRONIZER_PORT].GetInt();;

	return unique_ptr<Publisher>(new Publisher(&This::m_instance, publisherPort, synchronizerPort, name, numberOfSubscribers));
}


const std::string& Publisher::getName() const {
	return m_impl->getName();
}

const std::string& Publisher::getApplicationName() const {
	return m_impl->getApplicationName();
}

int Publisher::getApplicationId() const {
	return m_impl->getApplicationId();
}

const std::string& Publisher::getApplicationEndpoint() const {
	return m_impl->getApplicationEndpoint();
}

bool Publisher::waitForSubscribers() const {
	return m_impl->waitForSubscribers();
}

void Publisher::cancelWaitForSubscribers() {
	m_waiting->cancel();
}

void Publisher::sendBinary(const std::string& data) const {
	m_impl->sendBinary(data);
}

void Publisher::send(const std::string& data) const {
	m_impl->send(data);
}

void Publisher::sendTwoBinaryParts(const std::string& data1, const std::string& data2) const {
	m_impl->sendTwoBinaryParts(data1, data2);
}

bool Publisher::hasEnded() const {
	return m_impl->isEnded();
}

bool Publisher::isEnded() const {
	return m_impl->isEnded();
}

void Publisher::sendEnd() const {
	m_impl->setEnd();
}

///////////////////////////////////////////////////////////////////////////
// Subscriber

Subscriber::Subscriber(Server * server, const std::string & url, int publisherPort, int synchronizerPort, const std::string & publisherName, int numberOfSubscribers, const std::string& instanceName, int instanceId, const std::string& instanceEndpoint, const std::string& statusEndpoint) :
	m_impl(new SubscriberImpl(server, url, publisherPort, synchronizerPort, publisherName, numberOfSubscribers, instanceName, instanceId, instanceEndpoint, statusEndpoint)) {
}

Subscriber::~Subscriber() {
}

std::unique_ptr<Subscriber> Subscriber::create(Instance & instance, const std::string& publisherName) {
	try {
		return instance.m_server->createSubscriber(instance.m_id, publisherName, instance.m_name);

	} catch (const SubscriberCreationException& e) {
		// the publisher does not exist, so we are waiting for it
	}

	// waiting for the publisher
	State lastState = instance.waitFor(publisherName);

	// state cannot be terminal or it means that the application has terminated that is not planned.
	if (lastState == SUCCESS
		|| lastState == STOPPED
		|| lastState == KILLED
		|| lastState == FAILURE) {
		return unique_ptr<Subscriber>(nullptr);
	}

	try {
		return instance.m_server->createSubscriber(instance.m_id, publisherName, instance.m_name);

	} catch (const SubscriberCreationException& e) {
		// that should not happen
	}

	return unique_ptr<Subscriber>(nullptr);
}

void Subscriber::init() {
	m_impl->init();

	// Create the waiting here.
	m_waiting.reset(m_impl->waiting());
}

const std::string& Subscriber::getPublisherName() const {
	return m_impl->m_publisherName;
}

const std::string& Subscriber::getInstanceName() const {
	return m_impl->m_instanceName;
}

int Subscriber::getInstanceId() const {
	return m_impl->m_instanceId;
}

const std::string& Subscriber::getInstanceEndpoint() const {
	return m_impl->m_instanceEndpoint;
}

bool Subscriber::hasEnded() const {
	return m_impl->isEnded();
}

bool Subscriber::isEnded() const {
	return m_impl->isEnded();
}

bool Subscriber::isCanceled() const {
	return m_impl->isCanceled();
}

bool Subscriber::receiveBinary(std::string& data) const {
	return m_impl->receiveBinary(data);
}

bool Subscriber::receive(std::string& data) const {
	return m_impl->receive(data);
}

bool Subscriber::receiveTwoBinaryParts(std::string& data1, std::string& data2) const {
	return m_impl->receiveTwoBinaryParts(data1, data2);
}

void Subscriber::cancel() {
	m_waiting->cancel();
}

///////////////////////////////////////////////////////////////////////////
// Request

Request::Request(std::unique_ptr<RequestImpl> & impl) :
	m_impl(std::move(impl)) {
}

Request::~Request() {
}

std::string Request::getObjectId() const {

	// Local id is missing.
	return "request:"
		+ m_impl->m_requesterApplicationName
		+ "."
		+ to_string(m_impl->m_requesterApplicationId)
		+ "@"
		+ m_impl->m_requesterServerEndpoint;
}

std::string Request::getRequesterEndpoint() const {
	return m_impl->m_requesterServerEndpoint;
}

void Request::setTimeout(int value) {
	m_impl->setTimeout(value);
}

const std::string& Request::getBinary() const {
	return m_impl->m_message;
}

std::string Request::get() const {

	string data;
	parse(m_impl->m_message, data);

	return data;
}

const std::string& Request::getSecondBinaryPart() const {
	return m_impl->m_message2;
}

bool Request::replyBinary(const std::string& response) {
	return m_impl->replyBinary(response);
}

bool Request::reply(const std::string& response) {
	return m_impl->reply(response);
}

std::unique_ptr<Instance> Request::connectToRequester() {

	// Instantiate the requester server if it does not exist.
	if (m_requesterServer.get() == nullptr) {
		m_requesterServer.reset(new Server(m_impl->m_requesterServerEndpoint, m_impl->m_timeout));
	}

	// Connect and find the instance.
	InstanceArray instances = m_requesterServer->connectAll(m_impl->m_requesterApplicationName);

	for (int i = 0; i < instances.size(); i++) {
		if (instances[i]->getId() == m_impl->m_requesterApplicationId) {
			return unique_ptr<Instance>(std::move(instances[i]));
		}
	}

	// Not found.
	return unique_ptr<Instance>(nullptr);
}

std::unique_ptr<Server> Request::getServer() {
	return std::move(m_requesterServer);
}

///////////////////////////////////////////////////////////////////////////
// Responder

Responder::Responder(application::This * application, int responderPort, const std::string& name) :
	m_impl(new ResponderImpl(application, responderPort, name)) {

	// Create the waiting here.
	m_waiting.reset(m_impl->waiting());
}

Responder::~Responder() {
}

std::unique_ptr<Responder> Responder::create(const std::string& name) {

	string portName = ResponderImpl::RESPONDER_PREFIX + name;

	unique_ptr<zmq::message_t> reply = This::m_instance.m_requestSocket->request(This::m_instance.m_impl->createRequestPortV0Request(This::m_instance.m_id, portName));

	// Get the JSON response.
	json::Object response;
	json::parse(response, reply.get());

	int responderPort = response[message::RequestResponse::VALUE].GetInt();
	if (responderPort == -1) {
		throw ResponderCreationException(response[message::RequestResponse::MESSAGE].GetString());
	}

	return unique_ptr<Responder>(new Responder(&This::m_instance, responderPort, name));
}

const std::string& Responder::getName() const {
	return m_impl->m_name;
}

void Responder::cancel() {
	m_impl->cancel();
}

std::unique_ptr<Request> Responder::receive() {

	unique_ptr<RequestImpl> requestImpl = m_impl->receive();
	if (requestImpl.get() == nullptr) {
		return unique_ptr<Request>(nullptr);
	}
	return unique_ptr<Request>(new Request(requestImpl));
}

bool Responder::isCanceled() const {
	return m_impl->m_canceled;
}

///////////////////////////////////////////////////////////////////////////
// Requester

Requester::Requester(application::This * application, const std::string& url, int requesterPort, int responderPort, const std::string& name, int responderId, int requesterId) :
	m_impl(new RequesterImpl(application, url, requesterPort, responderPort, name, responderId, requesterId)) {

	// Create the waiting here.
	m_waiting.reset(m_impl->waiting());
}

Requester::~Requester() {
}

std::unique_ptr<Requester> Requester::create(Instance & instance, const std::string& name) {

	int responderId = instance.getId();
	string responderUrl = instance.getUrl();
	string responderEndpoint = instance.getEndpoint();

	// Create a request socket to the server of the instance.
	unique_ptr<RequestSocketImpl> instanceRequestSocket = This::m_instance.createRequestSocket(responderEndpoint);

	string responderPortName = ResponderImpl::RESPONDER_PREFIX + name;
	int requesterId = RequesterImpl::newRequesterId();
	string requesterPortName = RequesterImpl::getRequesterPortName(name, responderId, requesterId);

	string request = This::m_instance.m_impl->createConnectPortV0Request(responderId, responderPortName);

	unique_ptr<zmq::message_t> reply = instanceRequestSocket->request(request);

	// Get the JSON response.
	json::Object response;
	json::parse(response, reply.get());

	reply.reset();

	int responderPort = response[message::RequestResponse::VALUE].GetInt();
	if (responderPort == -1) {
		// Wait for the responder port.
		instance.waitFor(responderPortName);

		// Retry to connect.
		reply = instanceRequestSocket->request(request);
		json::parse(response, reply.get());

		responderPort = response[message::RequestResponse::VALUE].GetInt();
		if (responderPort == -1) {
			throw RequesterCreationException(response[message::RequestResponse::MESSAGE].GetString());
		}

		reply.reset();
	}

	// Request a requester port.
	reply = This::m_instance.m_requestSocket->request(This::m_instance.m_impl->createRequestPortV0Request(This::m_instance.m_id, requesterPortName));
	json::parse(response, reply.get());

	int requesterPort = response[message::RequestResponse::VALUE].GetInt();
	if (requesterPort == -1) {
		throw RequesterCreationException(response[message::RequestResponse::MESSAGE].GetString());
	}

	return unique_ptr<Requester>(new Requester(&This::m_instance, responderUrl, requesterPort, responderPort, name, responderId, requesterId));
}

const std::string& Requester::getName() const {
	return m_impl->m_name;
}

void Requester::sendBinary(const std::string& request) {
	m_impl->sendBinary(request);
}

void Requester::send(const std::string& request) {
	m_impl->send(request);
}

void Requester::sendTwoBinaryParts(const std::string& request1, const std::string& request2) {
	m_impl->sendTwoBinaryParts(request1, request2);
}

bool Requester::receiveBinary(std::string& response) {
	return m_impl->receiveBinary(response);
}

bool Requester::receive(std::string& response) {
	return m_impl->receive(response);
}

void Requester::cancel() {
	m_impl->cancel();
}

bool Requester::isCanceled() const {
	return m_impl->m_canceled;
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

std::string toString(cameo::application::State applicationStates) {

	vector<string> states;

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

	ostringstream result;

	for (int i = 0; i < states.size() - 1; i++) {
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

std::ostream& operator<<(std::ostream& os, const application::Publisher& publisher) {

	os << "pub." << publisher.getName()
		<< ":" << publisher.getApplicationName()
		<< "." << publisher.getApplicationId()
		<< "@" << publisher.getApplicationEndpoint();

	return os;
}

std::ostream& operator<<(std::ostream& os, const application::Subscriber& subscriber) {

	os << "sub." << subscriber.getPublisherName()
		<< ":" << subscriber.getInstanceName()
		<< "." << subscriber.getInstanceId()
		<< "@" << subscriber.getInstanceEndpoint();

	return os;
}

std::ostream& operator<<(std::ostream& os, const application::Request& request) {

	os << "[endpoint=" << request.m_impl->m_requesterEndpoint
			<< ", id=" << request.m_impl->m_requesterApplicationId << "]";

	return os;
}

std::ostream& operator<<(std::ostream& os, const application::Responder& responder) {

	os << "rep." << responder.getName()
		<< ":" << responder.m_impl->m_application->getName()
		<< "." << responder.m_impl->m_application->getId()
		<< "@" << responder.m_impl->m_application->getEndpoint();

	return os;
}

std::ostream& operator<<(std::ostream& os, const application::Requester& requester) {

	os << "req." << requester.getName()
		<< "." << requester.m_impl->m_requesterId
		<< ":" << requester.m_impl->m_application->getName()
		<< "." << requester.m_impl->m_application->getId()
		<< "@" << requester.m_impl->m_application->getEndpoint();

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

}
}
