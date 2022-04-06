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

#include "MultiResponder.h"
#include "ImplFactory.h"
#include "RequestSocket.h"
#include "Messages.h"
#include "Server.h"
#include "ContextZmq.h"
#include "Waiting.h"
#include "IdGenerator.h"
#include "impl/zmq/MultiResponderZmq.h"
#include "impl/zmq/MultiResponderRouterZmq.h"

namespace cameo {
namespace coms {
namespace multi {

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
		+ m_requesterServerEndpoint.toString();
}

std::string Request::getRequesterEndpoint() const {
	return m_requesterServerEndpoint.toString();
}

const std::string& Request::get() const {
	return m_messagePart1;
}

const std::string& Request::getSecondPart() const {
	return m_messagePart2;
}

Request::Request(const std::string & requesterApplicationName, int requesterApplicationId, const std::string& serverEndpoint, int serverProxyPort, const std::string& messagePart1, const std::string& messagePart2) :
	m_responder(nullptr),
	m_messagePart1(messagePart1),
	m_messagePart2(messagePart2),
	m_requesterApplicationName(requesterApplicationName),
	m_requesterApplicationId(requesterApplicationId),
	m_timeout(0) {

	m_requesterServerEndpoint = Endpoint::parse(serverEndpoint);
	m_requesterServerProxyPort = serverProxyPort;
}

bool Request::reply(const std::string& response) {

	json::StringObject jsonRequest;
	jsonRequest.pushKey(message::TYPE);
	jsonRequest.pushValue(message::RESPONSE);

	m_responder->reply(jsonRequest.dump(), response);

	return true;
}

ServerAndApp Request::connectToRequester(int options, bool useProxy) {

	ServerAndApp result;

	// Create the starter server.
	if (m_requesterServerEndpoint.getAddress() == "") {
		return {};
	}

	if (useProxy) {
		result.server = std::make_unique<Server>(m_requesterServerEndpoint.withPort(m_requesterServerProxyPort).toString(), options, true);
	}
	else {
		result.server = std::make_unique<Server>(m_requesterServerEndpoint.toString(), options, false);
	}

	// Iterate the instances to find the id
	AppArray instances = result.server->connectAll(m_requesterApplicationName, options);

	for (auto i = instances.begin(); i != instances.end(); ++i) {
		if ((*i)->getId() == m_requesterApplicationId) {
			result.app = std::unique_ptr<App>(std::move(*i));
			break;
		}
	}

	return result;
}

std::string Request::toString() const {

	return std::string("[id=") + std::to_string(m_requesterApplicationId) + "]";
}

///////////////////////////////////////////////////////////////////////////
// Responder Router

const std::string ResponderRouter::KEY = "responder-676e576d-6102-42d8-ae24-222a7000dfa0";
const std::string ResponderRouter::PORT = "port";

ResponderRouter::ResponderRouter(const std::string& name) :
	m_name(name) {

	m_impl = ImplFactory::createMultiResponderRouter();

	// Create the waiting here.
	m_waiting.reset(new Waiting(std::bind(&ResponderRouter::cancel, this)));
}

ResponderRouter::~ResponderRouter() {
	terminate();
}

void ResponderRouter::terminate() {

	if (m_impl) {
		This::getCom().removeKey(m_key);

		m_impl.reset();
	}
}

void ResponderRouter::setPollingTime(int value) {
	m_impl->setPollingTime(value);
}

void ResponderRouter::init(const std::string &name) {

	// Set the key.
	m_key = KEY + "-" + name;

	// Set the dealer endpoint.
	m_dealerEndpoint = std::string("inproc://") + IdGenerator::newStringId();

	// Init the responder socket.
	m_impl->init(StringId::from(This::getId(), m_key), m_dealerEndpoint);

	// Store the responder data.
	json::StringObject jsonData;

	jsonData.pushKey(PORT);
	jsonData.pushValue(m_impl->getResponderPort());

	try {
		This::getCom().storeKeyValue(m_key, jsonData.dump());
	}
	catch (const KeyAlreadyExistsException& e) {
		throw ResponderCreationException("A responder with the name \"" + name + "\" already exists");
	}
}

const std::string& ResponderRouter::getDealerEndpoint() const {
	return m_dealerEndpoint;
}

std::unique_ptr<ResponderRouter> ResponderRouter::create(const std::string& name) {

	std::unique_ptr<ResponderRouter> responder(new ResponderRouter(name));
	responder->init(name);

	return responder;
}

const std::string& ResponderRouter::getName() const {
	return m_name;
}

void ResponderRouter::cancel() {
	m_impl->cancel();
}

void ResponderRouter::run() {

	m_impl->run();
}

bool ResponderRouter::isCanceled() const {
	return m_impl->isCanceled();
}

std::string ResponderRouter::toString() const {

	return std::string("repr.") + m_name
		+ ":" + This::getName()
		+ "." + std::to_string(This::getId())
		+ "@" + This::getEndpoint().toString();
}

///////////////////////////////////////////////////////////////////////////
// Responder

Responder::Responder() {

	m_impl = ImplFactory::createMultiResponder();

	// Create the waiting here.
	m_waiting.reset(new Waiting(std::bind(&Responder::cancel, this)));
}

Responder::~Responder() {
	terminate();
}

void Responder::terminate() {
	m_impl.reset();
}

void Responder::init(const std::string &endpoint) {

	// Init the responder socket.
	m_impl->init(endpoint);
}

std::unique_ptr<Responder> Responder::create(const ResponderRouter& router) {

	std::unique_ptr<Responder> responder(new Responder());
	responder->init(router.getDealerEndpoint());

	return responder;
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

std::string Responder::toString() const {

	return std::string("repm")
		+ ":" + This::getName()
		+ "." + std::to_string(This::getId())
		+ "@" + This::getEndpoint().toString();
}

std::ostream& operator<<(std::ostream& os, const Request& request) {

	os << request.toString();

	return os;
}

std::ostream& operator<<(std::ostream& os, const ResponderRouter& router) {

	os << router.toString();

	return os;
}

std::ostream& operator<<(std::ostream& os, const Responder& responder) {

	os << responder.toString();

	return os;
}

}
}
}

