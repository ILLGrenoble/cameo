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

#include "RequesterResponder.h"

#include "JSON.h"
#include "Server.h"
#include "../base/impl/zmq/ContextZmq.h"
#include "../base/Messages.h"
#include "../base/RequestSocket.h"
#include "impl/zmq/RequesterZmq.h"
#include "impl/zmq/ResponderZmq.h"

namespace cameo {
namespace coms {

///////////////////////////////////////////////////////////////////////////
// Request

Request::~Request() {
}

void Request::setTimeout(int value) {
	m_timeout = value;
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

Request::Request(const std::string & requesterApplicationName, int requesterApplicationId, const std::string& serverUrl, int serverPort, int requesterPort, const std::string& messagePart1, const std::string& messagePart2) :
	m_messagePart1(messagePart1),
	m_messagePart2(messagePart2),
	m_requesterApplicationName(requesterApplicationName),
	m_requesterApplicationId(requesterApplicationId),
	m_timeout(0) {

	std::stringstream requesterEndpoint;
	requesterEndpoint << serverUrl << ":" << requesterPort;
	m_requesterEndpoint = requesterEndpoint.str();

	std::stringstream requesterServerEndpoint;
	requesterServerEndpoint << serverUrl << ":" << serverPort;
	m_requesterServerEndpoint = requesterServerEndpoint.str();
}

bool Request::replyBinary(const std::string& response) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushValue(message::RESPONSE);

	// Create a request socket. It is created for each request that could be optimized.
	std::unique_ptr<RequestSocket> requestSocket = application::This::getCom().createRequestSocket(m_requesterEndpoint);

	try {
		requestSocket->request(request.toString(), response);
	}
	catch (const ConnectionTimeout&) {
		return false;
	}

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

	for (int i = 0; i < instances.size(); i++) {
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

Responder::Responder(const std::string& name) :
	m_name(name) {

	//TODO Replace with a factory.
	m_impl = std::unique_ptr<ResponderImpl>(new ResponderZmq());

	// Create the waiting here.
	m_waiting.reset(new Waiting(std::bind(&Responder::cancel, this)));
}

Responder::~Responder() {
	application::This::getCom().removePort(ResponderImpl::RESPONDER_PREFIX + m_name);
}

void Responder::init(const std::string &name) {

	std::string portName = ResponderImpl::RESPONDER_PREFIX + name;
	json::Object response = application::This::getCom().requestJSON(createRequestPortV0Request(application::This::getId(), portName));

	int responderPort = response[message::RequestResponse::VALUE].GetInt();
	if (responderPort == -1) {
		throw ResponderCreationException(response[message::RequestResponse::MESSAGE].GetString());
	}

	m_impl->init(responderPort);
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
	return m_impl->receive();
}

bool Responder::isCanceled() const {
	return m_impl->isCanceled();
}

///////////////////////////////////////////////////////////////////////////
// Requester

std::mutex Requester::m_mutex;
int Requester::m_requesterCounter = 0;

Requester::Requester() :
	m_requesterId(0),
	m_appId(0) {

	//TODO Replace with factory.
	m_impl = std::unique_ptr<RequesterImpl>(new RequesterZmq());

	// Create the waiting here.
	m_waiting.reset(new Waiting(std::bind(&Requester::cancel, this)));
}

Requester::~Requester() {

	application::This::getCom().removePort(getRequesterPortName(m_responderName, m_appId, m_requesterId));
}

int Requester::newRequesterId() {

	std::lock_guard<std::mutex> lock(m_mutex);
	m_requesterCounter++;

	return m_requesterCounter;
}

std::string Requester::getRequesterPortName(const std::string& responderName, int responderId, int requesterId) {

	std::stringstream requesterPortName;
	requesterPortName << RequesterImpl::REQUESTER_PREFIX << responderName << "." << responderId << "." << requesterId;

	return requesterPortName.str();
}

void Requester::init(application::Instance & app, const std::string &responderName) {

	m_responderName = responderName;
	m_appName = app.getName();
	m_appId = app.getId();
	m_appEndpoint = app.getEndpoint();

	std::string responderPortName = ResponderZmq::RESPONDER_PREFIX + responderName;
	m_requesterId = newRequesterId();
	std::string requesterPortName = getRequesterPortName(responderName, m_appId, m_requesterId);

	std::string request = createConnectPortV0Request(m_appId, responderPortName);

	json::Object response = app.getCom().requestJSON(request);

	int responderPort = response[message::RequestResponse::VALUE].GetInt();
	if (responderPort == -1) {
		// Wait for the responder port.
		app.waitFor(responderPortName);

		// Retry to connect.
		response = app.getCom().requestJSON(request);

		responderPort = response[message::RequestResponse::VALUE].GetInt();
		if (responderPort == -1) {
			throw RequesterCreationException(response[message::RequestResponse::MESSAGE].GetString());
		}
	}

	// Request a requester port.
	response = application::This::getCom().requestJSON(createRequestPortV0Request(application::This::getId(), requesterPortName));

	int requesterPort = response[message::RequestResponse::VALUE].GetInt();
	if (requesterPort == -1) {
		throw RequesterCreationException(response[message::RequestResponse::MESSAGE].GetString());
	}

	m_impl->init(app.getEndpoint(), requesterPort, responderPort);
}

std::unique_ptr<Requester> Requester::create(application::Instance & app, const std::string& responderName) {

	std::unique_ptr<Requester> requester = std::unique_ptr<Requester>(new Requester());
	requester->init(app, responderName);

	return requester;
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

std::ostream& operator<<(std::ostream& os, const Request& request) {

	os << "[endpoint=" << request.m_requesterEndpoint
			<< ", id=" << request.m_requesterApplicationId << "]";

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
		<< "." << requester.m_requesterId
		<< ":" << requester.m_appName
		<< "." << requester.m_appId
		<< "@" << requester.m_appEndpoint;

	return os;
}

}
}
