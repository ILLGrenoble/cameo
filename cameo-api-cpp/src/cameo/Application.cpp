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
#include "EventStreamSocket.h"
#include "impl/ApplicationImpl.h"
#include "impl/PublisherImpl.h"
#include "impl/RequesterImpl.h"
#include "impl/RequestImpl.h"
#include "impl/ResponderImpl.h"
#include "impl/SocketImpl.h"
#include "impl/SubscriberImpl.h"
#include "impl/WaitingImpl.h"
#include "impl/WaitingImplSet.h"
#include "PortEvent.h"
#include "ProtoType.h"
#include "PublisherEvent.h"
#include "ResultEvent.h"
#include "Server.h"
#include "StarterServerException.h"
#include "StatusEvent.h"

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
	if (m_instance.m_impl == 0) {
		m_instance.initApplication(argc, argv);
	}
}

std::string This::getReference() {
	if (m_instance.m_impl != 0) {
		ostringstream os;
		os << getName() << "." << getId() << "@" << getEndpoint();
		return os.str();
	}
	return "";
}

void This::terminate() {

	// Test if termination is already done.
	if (m_instance.m_impl == 0) {
		return;
	}

	// Terminate the unmanaged application.
	if (!m_instance.m_managed) {
		m_instance.terminateUnmanagedApplication();
	}

	// Terminate the services.
	m_instance.Services::terminate();

	// Ensure that it won't be done twice.
	m_instance.m_impl = 0;
}


This::This() :
	Services(),
	m_impl(0),
	m_id(-1),
	m_managed(false),
	m_starterId(0) {
}

void This::initApplication(int argc, char *argv[]) {

	m_impl = new ApplicationImpl();
	Services::setImpl(m_impl);

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
	m_server = auto_ptr<Server>(new Server(m_serverEndpoint));

	// Create the starter server
	if (m_starterEndpoint != "") {
		m_starterServer = auto_ptr<Server>(new Server(m_starterEndpoint));
	}

	m_waitingSet = auto_ptr<WaitingImplSet>(new WaitingImplSet());
}

This::~This() {
	// Do not delete the impl here because there will be order trouble.

	// Terminate the unmanaged application.
	if (m_impl != 0 && !m_managed) {
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
	if (m_instance.m_impl != 0) {
		return m_instance.m_serverEndpoint;
	}
	static string result;
	return result;
}

Server& This::getServer() {
	return *m_instance.m_server;
}

Server& This::getStarterServer() {

	if (m_instance.m_starterServer.get() == 0) {
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

	string strRequestType = m_impl->createRequest(PROTO_STARTEDUNMANAGED);
	string strRequestData = m_impl->createStartedUnmanagedRequest(m_name);
	auto_ptr<zmq::message_t> reply = m_impl->tryRequestWithOnePartReply(strRequestType, strRequestData, m_serverEndpoint);

	proto::RequestResponse requestResponse;
	requestResponse.ParseFromArray((*reply).data(), (*reply).size());

	return requestResponse.value();
}

void This::terminateUnmanagedApplication() {

	string strRequestType = m_impl->createRequest(PROTO_TERMINATEDUNMANAGED);
	string strRequestData = m_impl->createTerminatedUnmanagedRequest(m_id);
	auto_ptr<zmq::message_t> reply = m_impl->tryRequestWithOnePartReply(strRequestType, strRequestData, m_serverEndpoint);

	proto::RequestResponse requestResponse;
	requestResponse.ParseFromArray((*reply).data(), (*reply).size());
}

bool This::setRunning() {

	string strRequestType = m_instance.m_impl->createRequest(PROTO_SETSTATUS);
	string strRequestData = m_instance.m_impl->createSetStatusRequest(m_instance.m_id, RUNNING);
	auto_ptr<zmq::message_t> reply = m_instance.m_impl->tryRequestWithOnePartReply(strRequestType, strRequestData, m_instance.m_serverEndpoint);

	proto::RequestResponse requestResponse;
	requestResponse.ParseFromArray((*reply).data(), (*reply).size());

	if (requestResponse.value() == -1) {
		return false;
	}

	return true;
}

void This::setBinaryResult(const std::string& data) {

	string strRequestType = m_instance.m_impl->createRequest(PROTO_SETRESULT);
	string strRequestData = m_instance.m_impl->createSetResultRequest(m_instance.m_id, data);

	auto_ptr<zmq::message_t> reply = m_instance.m_impl->tryRequestWithOnePartReply(strRequestType, strRequestData, m_instance.m_serverEndpoint);
	proto::RequestResponse requestResponse;
	requestResponse.ParseFromArray((*reply).data(), (*reply).size());

	if (requestResponse.value() == -1) {
		//throw ?;
		// Unexpected exception
	}
}

void This::setResult(const std::string& data) {

	string resultData;
	serialize(data, resultData);
	setBinaryResult(resultData);
}

void This::setResult(const int32_t* data, std::size_t size) {

	string resultData;
	serialize(data, size, resultData);
	setBinaryResult(resultData);
}

void This::setResult(const int64_t* data, std::size_t size) {

	string resultData;
	serialize(data, size, resultData);
	setBinaryResult(resultData);
}

void This::setResult(const float* data, std::size_t size) {

	string resultData;
	serialize(data, size, resultData);
	setBinaryResult(resultData);
}

void This::setResult(const double* data, std::size_t size) {

	string resultData;
	serialize(data, size, resultData);
	setBinaryResult(resultData);
}

State This::getState(int id) const {

	string strRequestType = m_impl->createRequest(PROTO_GETSTATUS);
	string strRequestData = m_impl->createGetStatusRequest(id);
	auto_ptr<zmq::message_t> reply = m_impl->tryRequestWithOnePartReply(strRequestType, strRequestData, m_serverEndpoint);

	proto::StatusEvent protoStatus;
	protoStatus.ParseFromArray((*reply).data(), (*reply).size());

	return protoStatus.applicationstate();
}

bool This::destroyPublisher(const std::string& name) const {

	string strRequestType = m_impl->createRequest(PROTO_TERMINATEPUBLISHER);
	string strRequestData = m_impl->createTerminatePublisherRequest(m_id, name);
	auto_ptr<zmq::message_t> reply = m_impl->tryRequestWithOnePartReply(strRequestType, strRequestData, m_serverEndpoint);

	proto::RequestResponse requestResponse;
	requestResponse.ParseFromArray((*reply).data(), (*reply).size());

	int value = requestResponse.value();
	return (value != -1);
}

bool This::removePort(const std::string& name) const {

	string strRequestType = m_impl->createRequest(PROTO_REMOVEPORT);
	string strRequestData = m_impl->createRemovePortRequest(m_id, name);
	auto_ptr<zmq::message_t> reply = m_impl->tryRequestWithOnePartReply(strRequestType, strRequestData, m_serverEndpoint);

	proto::RequestResponse requestResponse;
	requestResponse.ParseFromArray((*reply).data(), (*reply).size());

	int value = requestResponse.value();
	return (value != -1);
}

State This::waitForStop() {

	// open the event stream
	auto_ptr<EventStreamSocket> socket = openEventStream();
	m_impl->setEventSocket(socket);

	// test if stop was requested elsewhere
	State state = getState(m_id);
	if (state == STOPPING
		|| state == KILLING) {
		return state;
	}

	while (true) {
		// waits for a new incoming status
		auto_ptr<Event> event = m_impl->m_eventSocket->receive();

		// The socket is canceled.
		if (event.get() == 0) {
			return UNKNOWN;
		}

		if (event->getId() == m_id) {
			StatusEvent * status = dynamic_cast<StatusEvent *>(event.get());

			if (status != 0) {
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

std::auto_ptr<Instance> This::connectToStarter() {

	if (m_instance.m_starterServer.get() == 0) {
		return auto_ptr<Instance>(0);
	}

	// Iterate the instances to find the id
	InstanceArray instances = m_instance.m_starterServer->connectAll(m_instance.m_starterName);

	for (int i = 0; i < instances.size(); i++) {
		if (instances[i]->getId() == m_instance.m_starterId) {
			return auto_ptr<Instance>(instances[i]);
		}
	}

	return auto_ptr<Instance>(0);
}

void This::handleStopImpl(StopFunctionType function) {
	m_impl->handleStop(&m_instance, function);
}

///////////////////////////////////////////////////////////////////////////////
// Instance

Instance::Instance(const Server * server, std::auto_ptr<EventStreamSocket>& socket) :
	m_server(server),
	m_eventSocket(socket),
	m_id(-1),
	m_pastStates(0),
	m_initialState(UNKNOWN),
	m_lastState(UNKNOWN),
	m_hasResult(false) {

	// Create the waiting.
	m_waiting.reset(m_eventSocket->waiting());
}

Instance::~Instance() {
	// the destructor has been added to avoid blocking ZeroMQ, because the inner objects destructors were not called.
}

void Instance::setId(int id) {
	m_id = id;
}

void Instance::setName(const std::string& name) {
	m_name = name;
}

void Instance::setErrorMessage(const std::string& message) {
	m_errorMessage = message;
}

void Instance::setPastStates(State pastStates) {
	m_pastStates = pastStates;
}

void Instance::setInitialState(State state) {
	m_initialState = state;

	// It is important to set the last state, because in case of a call to the function now without any incoming state.
	m_lastState = state;
}

const std::string& Instance::getName() const {
	return m_name;
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

State Instance::waitFor(int states, const std::string& eventName, StateHandlerType handler, bool blocking) {

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
		auto_ptr<Event> event = m_eventSocket->receive(blocking);

		// The socket is canceled or the non-blocking call returns a null message.
		if (event.get() == 0) {
			return m_lastState;
		}

		if (event->getId() == m_id) {
			StatusEvent * status = dynamic_cast<StatusEvent *>(event.get());

			if (status != 0) {
				State state = status->getState();
				m_pastStates = status->getPastStates();
				m_lastState = state;

				// Call the state handler.
				if (!handler.empty()) {
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

			} else {

				if (ResultEvent * result = dynamic_cast<ResultEvent *>(event.get())) {
					m_hasResult = true;
					m_resultData = result->getData();

				} else if (PublisherEvent * publisher = dynamic_cast<PublisherEvent *>(event.get())) {
					if (publisher->getPublisherName() == eventName) {
						break;
					}

				} else if (PortEvent * port = dynamic_cast<PortEvent *>(event.get())) {
					if (port->getPortName() == eventName) {
						break;
					}
				}
			}

		}
	}

	return m_lastState;
}

State Instance::waitFor(int states, const std::string& eventName, StateHandlerType handler) {
	return waitFor(states, eventName, handler, true);
}

State Instance::waitFor(int states, StateHandlerType handler) {
	return waitFor(states, "", handler);
}

State Instance::waitFor(StateHandlerType handler) {
	return waitFor(0, "", handler);
}

void Instance::cancelWaitFor() {
	m_waiting->cancel();
}

State Instance::now() {
	return waitFor(0, "", 0, false);
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

bool Instance::getResult(std::vector<int32_t>& result) {

	string bytes;
	getBinaryResult(bytes);
	parse(bytes, result);

	return m_hasResult;
}

bool Instance::getResult(std::vector<int64_t>& result) {

	string bytes;
	getBinaryResult(bytes);
	parse(bytes, result);

	return m_hasResult;
}

bool Instance::getResult(std::vector<float>& result) {

	string bytes;
	getBinaryResult(bytes);
	parse(bytes, result);

	return m_hasResult;
}

bool Instance::getResult(std::vector<double>& result) {

	string bytes;
	getBinaryResult(bytes);
	parse(bytes, result);

	return m_hasResult;
}

///////////////////////////////////////////////////////////////////////////
// InstanceArray

InstanceArray::InstanceArray() :
	m_size(0),
	m_array(0) {
}

InstanceArray::InstanceArray(const InstanceArray& array) :
	m_size(array.m_size),
	m_array(new auto_ptr<Instance>[m_size]) {

	// transferring pointers
	for (size_t i = 0; i < m_size; i++) {
		m_array[i] = array.m_array[i];
	}
}

InstanceArray::~InstanceArray() {
	delete [] m_array;
}

void InstanceArray::allocate(std::size_t size) {
	m_size = size;
	m_array = new auto_ptr<Instance>[size];
}

std::size_t InstanceArray::size() const {
	return m_size;
}

std::auto_ptr<Instance>& InstanceArray::operator[](std::size_t index) {
	return m_array[index];
}

///////////////////////////////////////////////////////////////////////////////
// Publisher

Publisher::Publisher(const This * application, int publisherPort, int synchronizerPort, const std::string& name, int numberOfSubscribers) :
	m_impl(new PublisherImpl(application, publisherPort, synchronizerPort, name, numberOfSubscribers)) {

	// Create the waiting here.
	m_waiting.reset(m_impl->waiting());
}

Publisher::~Publisher() {
}

std::auto_ptr<Publisher> Publisher::create(const std::string& name, int numberOfSubscribers) {

	string strRequestType = This::m_instance.m_impl->createRequest(PROTO_CREATEPUBLISHER);
	string strRequestData = This::m_instance.m_impl->createCreatePublisherRequest(This::m_instance.m_id, name, numberOfSubscribers);

	auto_ptr<zmq::message_t> reply = This::m_instance.m_impl->tryRequestWithOnePartReply(strRequestType, strRequestData, This::m_instance.m_serverEndpoint);
	proto::PublisherResponse requestResponse;
	requestResponse.ParseFromArray((*reply).data(), (*reply).size());

	int publisherPort = requestResponse.publisherport();
	if (publisherPort == -1) {
		throw PublisherCreationException(requestResponse.message());
	}
	int synchronizerPort = requestResponse.synchronizerport();

	return auto_ptr<Publisher>(new Publisher(&This::m_instance, publisherPort, synchronizerPort, name, numberOfSubscribers));
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

void Publisher::send(const int32_t* data, std::size_t size) const {
	m_impl->send(data, size);
}

void Publisher::send(const int64_t* data, std::size_t size) const {
	m_impl->send(data, size);
}

void Publisher::send(const float* data, std::size_t size) const {
	m_impl->send(data, size);
}

void Publisher::send(const double* data, std::size_t size) const {
	m_impl->send(data, size);
}

void Publisher::sendTwoBinaryParts(const std::string& data1, const std::string& data2) const {
	m_impl->sendTwoBinaryParts(data1, data2);
}

bool Publisher::hasEnded() const {
	return m_impl->hasEnded();
}

void Publisher::sendEnd() const {
	m_impl->setEnd();
}

///////////////////////////////////////////////////////////////////////////
// Subscriber

Subscriber::Subscriber(const Server * server, const std::string & url, int publisherPort, int synchronizerPort, const std::string & publisherName, int numberOfSubscribers, const std::string& instanceName, int instanceId, const std::string& instanceEndpoint, const std::string& statusEndpoint) :
	m_impl(new SubscriberImpl(server, url, publisherPort, synchronizerPort, publisherName, numberOfSubscribers, instanceName, instanceId, instanceEndpoint, statusEndpoint)) {
}

Subscriber::~Subscriber() {
}

std::auto_ptr<Subscriber> Subscriber::create(Instance & instance, const std::string& publisherName) {
	try {
		return instance.m_server->createSubscriber(instance.m_id, publisherName, instance.m_name);

	} catch (const SubscriberCreationException& e) {
		// the publisher does not exist, so we are waiting for it
	}

	// waiting for the publisher
	State lastState = instance.waitFor(0, publisherName);

	// state cannot be terminal or it means that the application has terminated that is not planned.
	if (lastState == SUCCESS
		|| lastState == STOPPED
		|| lastState == KILLED
		|| lastState == FAILURE) {
		return auto_ptr<Subscriber>(0);
	}

	try {
		return instance.m_server->createSubscriber(instance.m_id, publisherName, instance.m_name);

	} catch (const SubscriberCreationException& e) {
		// that should not happen
	}

	return auto_ptr<Subscriber>(0);
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
	return m_impl->hasEnded();
}

bool Subscriber::receiveBinary(std::string& data) const {
	return m_impl->receiveBinary(data);
}

bool Subscriber::receive(std::string& data) const {
	return m_impl->receive(data);
}

bool Subscriber::receive(std::vector<int32_t>& data) const {
	return m_impl->receive(data);
}

bool Subscriber::receive(std::vector<int64_t>& data) const {
	return m_impl->receive(data);
}

bool Subscriber::receive(std::vector<float>& data) const {
	return m_impl->receive(data);
}

bool Subscriber::receive(std::vector<double>& data) const {
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

Request::Request(std::auto_ptr<RequestImpl> & impl) :
	m_impl(impl) {
}

Request::~Request() {
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

void Request::replyBinary(const std::string& response) {
	m_impl->replyBinary(response);
}

void Request::reply(const std::string& response) {
	m_impl->reply(response);
}

std::auto_ptr<Instance> Request::connectToRequester() {

	// Instantiate the requester server if it does not exist.
	if (m_requesterServer.get() == 0) {
		m_requesterServer.reset(new Server(m_impl->m_requesterServerEndpoint));
	}

	// Connect and find the instance.
	InstanceArray instances = m_requesterServer->connectAll(m_impl->m_requesterApplicationName);

	for (int i = 0; i < instances.size(); i++) {
		if (instances[i]->getId() == m_impl->m_requesterApplicationId) {
			return auto_ptr<Instance>(instances[i]);
		}
	}

	// Not found.
	return auto_ptr<Instance>(0);
}

std::auto_ptr<Server> Request::getServer() {
	return m_requesterServer;
}

///////////////////////////////////////////////////////////////////////////
// Responder

Responder::Responder(const application::This * application, int responderPort, const std::string& name) :
	m_impl(new ResponderImpl(application, responderPort, name)) {

	// Create the waiting here.
	m_waiting.reset(m_impl->waiting());
}

Responder::~Responder() {
}

std::auto_ptr<Responder> Responder::create(const std::string& name) {

	string portName = ResponderImpl::RESPONDER_PREFIX + name;

	string strRequestType = This::m_instance.m_impl->createRequest(PROTO_REQUESTPORT);
	string strRequestData = This::m_instance.m_impl->createRequestPortRequest(This::m_instance.m_id, portName);

	auto_ptr<zmq::message_t> reply = This::m_instance.m_impl->tryRequestWithOnePartReply(strRequestType, strRequestData, This::m_instance.m_serverEndpoint);
	proto::RequestResponse requestResponse;
	requestResponse.ParseFromArray((*reply).data(), (*reply).size());

	int responderPort = requestResponse.value();
	if (responderPort == -1) {
		throw ResponderCreationException(requestResponse.message());
	}

	return auto_ptr<Responder>(new Responder(&This::m_instance, responderPort, name));
}

const std::string& Responder::getName() const {
	return m_impl->m_name;
}

void Responder::cancel() {
	m_impl->cancel();
}

std::auto_ptr<Request> Responder::receive() {

	auto_ptr<RequestImpl> requestImpl = m_impl->receive();
	if (requestImpl.get() == 0) {
		return auto_ptr<Request>(0);
	}
	return auto_ptr<Request>(new Request(requestImpl));
}

bool Responder::hasEnded() const {
	return m_impl->m_ended;
}

///////////////////////////////////////////////////////////////////////////
// Requester

Requester::Requester(const application::This * application, const std::string& url, int requesterPort, int responderPort, const std::string& name, int responderId, int requesterId) :
	m_impl(new RequesterImpl(application, url, requesterPort, responderPort, name, responderId, requesterId)) {

	// Create the waiting here.
	m_waiting.reset(m_impl->waiting());
}

Requester::~Requester() {
}

std::auto_ptr<Requester> Requester::create(Instance & instance, const std::string& name) {

	int responderId = instance.getId();
	string responderUrl = instance.getUrl();
	string responderEndpoint = instance.getEndpoint();

	string responderPortName = ResponderImpl::RESPONDER_PREFIX + name;
	int requesterId = RequesterImpl::newRequesterId();
	string requesterPortName = RequesterImpl::getRequesterPortName(name, responderId, requesterId);

	string strRequestType = This::m_instance.m_impl->createRequest(PROTO_CONNECTPORT);
	string strRequestData = This::m_instance.m_impl->createConnectPortRequest(responderId, responderPortName);

	auto_ptr<zmq::message_t> reply = This::m_instance.m_impl->tryRequestWithOnePartReply(strRequestType, strRequestData, responderEndpoint);
	proto::RequestResponse requestResponse;
	requestResponse.ParseFromArray((*reply).data(), (*reply).size());
	reply.reset();

	int responderPort = requestResponse.value();
	if (responderPort == -1) {
		// Wait for the responder port.
		instance.waitFor(0, responderPortName);

		// Retry to connect.
		reply = This::m_instance.m_impl->tryRequestWithOnePartReply(strRequestType, strRequestData, responderEndpoint);
		requestResponse.ParseFromArray((*reply).data(), (*reply).size());

		responderPort = requestResponse.value();
		if (responderPort == -1) {
			throw RequesterCreationException(requestResponse.message());
		}

		reply.reset();
	}

	// Request a requester port
	strRequestType = This::m_instance.m_impl->createRequest(PROTO_REQUESTPORT);
	strRequestData = This::m_instance.m_impl->createRequestPortRequest(This::m_instance.m_id, requesterPortName);

	reply = This::m_instance.m_impl->tryRequestWithOnePartReply(strRequestType, strRequestData, This::m_instance.m_serverEndpoint);
	requestResponse.ParseFromArray((*reply).data(), (*reply).size());

	int requesterPort = requestResponse.value();
	if (requesterPort == -1) {
		throw RequesterCreationException(requestResponse.message());
	}

	return auto_ptr<Requester>(new Requester(&This::m_instance, responderUrl, requesterPort, responderPort, name, responderId, requesterId));
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

///////////////////////////////////////////////////////////////////////////
// Configuration

Configuration::Configuration(const std::string& name, const std::string& description, bool singleInstance, bool restart, int startingTime, int retries, int stoppingTime) {

	m_name = name;
	m_description = description;
	m_singleInstance = singleInstance;
	m_restart = restart;
	m_startingTime = startingTime;
	m_retries = retries;
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

int Configuration::getRetries() const {
	return m_retries;
}

int Configuration::getStoppingTime() const {
	return m_stoppingTime;
}

///////////////////////////////////////////////////////////////////////////
// Info

Info::Info(const std::string& name, int id, State applicationState, State pastApplicationStates, const std::string& args) :
	m_id(id),
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
			<< ", retries=" << info.m_retries
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