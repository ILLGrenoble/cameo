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

#include "BasicRequesterResponder.h"
#include "JSON.h"
#include "Server.h"
#include "../base/impl/zmq/ContextZmq.h"
#include "../base/Messages.h"
#include "../base/RequestSocket.h"
#include "../factory/ImplFactory.h"
#include "impl/zmq/BasicRequesterZmq.h"
#include "impl/zmq/BasicResponderZmq.h"

namespace cameo {
namespace coms {
namespace basic {

///////////////////////////////////////////////////////////////////////////
// Request

Request::~Request() {
}

void Request::setTimeout(int value) {
	m_timeout = value;
}

void Request::setResponder(Responder* responder) {
	// Be careful with the pointer, the responder must not be deleted before using this request.
	m_responder = responder;
}

std::string Request::getObjectId() const {

	// Local id is missing.
	return "request:"
		+ m_requesterApplicationName
		+ "."
		+ std::to_string(m_requesterApplicationId)
		+ "@"
		+ m_requesterServerEndpoint;
}

std::string Request::getRequesterEndpoint() const {
	return m_requesterServerEndpoint;
}

const std::string& Request::getBinary() const {
	return m_messagePart1;
}

std::string Request::get() const {

	std::string data;
	parse(m_messagePart1, data);

	return data;
}

const std::string& Request::getSecondBinaryPart() const {
	return m_messagePart2;
}

Request::Request(const std::string & requesterApplicationName, int requesterApplicationId, const std::string& serverUrl, int serverPort, const std::string& messagePart1, const std::string& messagePart2) :
	m_responder(nullptr),
	m_messagePart1(messagePart1),
	m_messagePart2(messagePart2),
	m_requesterApplicationName(requesterApplicationName),
	m_requesterApplicationId(requesterApplicationId),
	m_timeout(0) {

	std::stringstream requesterServerEndpoint;
	requesterServerEndpoint << serverUrl << ":" << serverPort;
	m_requesterServerEndpoint = requesterServerEndpoint.str();
}

bool Request::replyBinary(const std::string& response) {

	json::StringObject jsonRequest;
	jsonRequest.pushKey(message::TYPE);
	jsonRequest.pushValue(message::RESPONSE);

	m_responder->reply(jsonRequest.toString(), response);

	return true;
}

bool Request::reply(const std::string& response) {

	// Encode the data.
	std::string result;
	serialize(response, result);

	return replyBinary(result);
}

std::unique_ptr<application::Instance> Request::connectToRequester() {

	// Instantiate the requester server if it does not exist.
	if (m_requesterServer.get() == nullptr) {
		m_requesterServer.reset(new Server(m_requesterServerEndpoint, m_timeout));
	}

	// Connect and find the instance.
	application::InstanceArray instances = m_requesterServer->connectAll(m_requesterApplicationName);

	for (size_t i = 0; i < instances.size(); i++) {
		if (instances[i]->getId() == m_requesterApplicationId) {
			return std::unique_ptr<application::Instance>(std::move(instances[i]));
		}
	}

	// Not found.
	return std::unique_ptr<application::Instance>(nullptr);
}

std::unique_ptr<Server> Request::getServer() {
	return std::move(m_requesterServer);
}

///////////////////////////////////////////////////////////////////////////
// Responder

const std::string Responder::KEY = "responder-676e576d-6102-42d8-ae24-222a7000dfa0";
const std::string Responder::PORT = "port";

Responder::Responder(const std::string& name) :
	m_name(name) {

	m_impl = ImplFactory::createBasicResponder();

	// Create the waiting here.
	m_waiting.reset(new Waiting(std::bind(&Responder::cancel, this)));
}

Responder::~Responder() {

	application::This::getCom().removeKey(m_key);
}

void Responder::init(const std::string &name) {

	// Set the key.
	m_key = KEY + "-" + name;

	// Init the reponder socket.
	m_impl->init(StringId::from(application::This::getId(), m_key));

	// Store the responder data.
	json::StringObject jsonData;

	jsonData.pushKey(PORT);
	jsonData.pushValue(m_impl->getResponderPort());

	try {
		application::This::getCom().storeKeyValue(m_key, jsonData.toString());
	}
	catch (const KeyAlreadyExistsException& e) {
		throw ResponderCreationException("A responder with the name \"" + name + "\" already exists");
	}
}

std::unique_ptr<Responder> Responder::create(const std::string& name) {

	std::unique_ptr<Responder> responder(new Responder(name));
	responder->init(name);

	return responder;
}

const std::string& Responder::getName() const {
	return m_name;
}

void Responder::cancel() {
	m_impl->cancel();
}

std::unique_ptr<Request> Responder::receive() {

	// Receive the request.
	std::unique_ptr<Request> request = m_impl->receive();

	// Do not set the responder if the request is null which happens after a cancel.
	if (request) {
		request->setResponder(this);
	}

	return request;
}

void Responder::reply(const std::string& type, const std::string& response) {
	m_impl->reply(type, response);
}

bool Responder::isCanceled() const {
	return m_impl->isCanceled();
}

///////////////////////////////////////////////////////////////////////////
// Requester

Requester::Requester() :
	m_useProxy(true),
	m_appId(0) {

	m_impl = ImplFactory::createBasicRequester();

	// Create the waiting here.
	m_waiting.reset(new Waiting(std::bind(&Requester::cancel, this)));
}

Requester::~Requester() {
}

void Requester::tryInit(application::Instance & app) {

	// Get the responder data.
	try {
		std::string jsonString = app.getCom().getKeyValue(m_key);

		json::Object jsonData;
		json::parse(jsonData, jsonString);

		int responderPort = jsonData[Responder::PORT.c_str()].GetInt();

		Endpoint endpoint;

		// The endpoint depends on the use of the proxy.
		if (m_useProxy) {
			endpoint = app.getEndpoint();
		}
		else {
			endpoint = app.getEndpoint().withPort(responderPort);
		}

		m_impl->init(endpoint, StringId::from(m_appId, m_key));
	}
	catch (...) {
		throw RequesterCreationException("Cannot create requester");
	}
}

void Requester::init(application::Instance & app, const std::string &responderName) {

	m_responderName = responderName;
	m_appName = app.getName();
	m_appId = app.getId();
	m_appEndpoint = app.getEndpoint();
	m_key = Responder::KEY + "-" + responderName;

	try {
		return tryInit(app);
	}
	catch (...) {
		// The responder does not exist so we are waiting for it.
	}

	// Wait for the responder.
	KeyValue keyValue(m_key);
	application::State lastState = app.waitFor(keyValue);

	// The state cannot be terminal or it means that the application has terminated.
	if (lastState == application::SUCCESS
		|| lastState == application::STOPPED
		|| lastState == application::KILLED
		|| lastState == application::FAILURE) {
		throw RequesterCreationException("Cannot create requester");
	}

	try {
		tryInit(app);
	}
	catch (...) {
		// Should not happen.
	}
}

std::unique_ptr<Requester> Requester::create(application::Instance & app, const std::string& responderName) {

	std::unique_ptr<Requester> requester = std::unique_ptr<Requester>(new Requester());
	requester->init(app, responderName);

	return requester;
}

void Requester::setPollingTime(int value) {
	m_impl->setPollingTime(value);
}

void Requester::setTimeout(int value) {
	m_impl->setTimeout(value);
}

const std::string& Requester::getResponderName() const {
	return m_responderName;
}

const std::string& Requester::getAppName() const {
	return m_appName;
}

int Requester::getAppId() const {
	return m_appId;
}

Endpoint Requester::getAppEndpoint() const {
	return m_appEndpoint;
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

std::optional<std::string> Requester::receiveBinary() {
	return m_impl->receiveBinary();
}

std::optional<std::string> Requester::receive() {
	return m_impl->receive();
}

void Requester::cancel() {
	m_impl->cancel();
}

bool Requester::isCanceled() const {
	return m_impl->isCanceled();
}

bool Requester::hasTimedout() const {
	return m_impl->hasTimedout();
}

std::ostream& operator<<(std::ostream& os, const Request& request) {

	os << "[id=" << request.m_requesterApplicationId << "]";

	return os;
}

std::ostream& operator<<(std::ostream& os, const Responder& responder) {

	os << "rep." << responder.m_name
		<< ":" << application::This::getName()
		<< "." << application::This::getId()
		<< "@" << application::This::getEndpoint();

	return os;
}

std::ostream& operator<<(std::ostream& os, const Requester& requester) {

	os << "req." << requester.m_responderName
		<< ":" << requester.m_appName
		<< "." << requester.m_appId
		<< "@" << requester.m_appEndpoint;

	return os;
}

}
}
}

