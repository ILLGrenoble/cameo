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

#include "This.h"
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

void Request::setResponder(Responder* responder) {
	// Be careful with the pointer, the responder must not be deleted before using this request.
	m_responder = responder;
}

std::string Request::getRequesterEndpoint() const {
	return m_requesterServerEndpoint.toString();
}

const std::string& Request::get() const {
	return m_messagePart1;
}

const std::string& Request::getFirstPart() const {
	return m_messagePart1;
}

const std::string& Request::getSecondPart() const {
	return m_messagePart2;
}

Request::Request(const std::string & requesterApplicationName, int requesterApplicationId, const std::string& serverEndpoint, int serverProxyPort, const std::string& messagePart1, const std::string& messagePart2) :
	m_responder{nullptr},
	m_messagePart1{messagePart1},
	m_messagePart2{messagePart2},
	m_requesterApplicationName{requesterApplicationName},
	m_requesterApplicationId{requesterApplicationId} {

	m_requesterServerEndpoint = Endpoint::parse(serverEndpoint);
	m_requesterServerProxyPort = serverProxyPort;
}

void Request::reply(const std::string& response) {

	json::StringObject jsonRequest;
	jsonRequest.pushKey(message::TYPE);
	jsonRequest.pushValue(message::RESPONSE);

	m_responder->reply(jsonRequest.dump(), response);
}

std::unique_ptr<ServerAndApp> Request::connectToRequester(int options, int timeout) {

	// Create the starter server.
	if (m_requesterServerEndpoint.getAddress() == "") {
		return {};
	}

	std::unique_ptr<Server> server;
	std::unique_ptr<App> app;

	bool useProxy = ((options & option::USE_PROXY) != 0);
	if (useProxy) {
		server = Server::create(m_requesterServerEndpoint.withPort(m_requesterServerProxyPort).toString(), option::USE_PROXY);
	}
	else {
		server = Server::create(m_requesterServerEndpoint.toString());
	}

	// Set the server init timeout.
	server->setTimeout(timeout);

	try {
		// Init the server.
		server->init();

		// Iterate the instances to find the id.
		AppArray instances = server->connectAll(m_requesterApplicationName, options);

		for (auto i = instances.begin(); i != instances.end(); ++i) {
			if ((*i)->getId() == m_requesterApplicationId) {
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

std::string Request::toString() const {

	json::StringObject jsonObject;

	jsonObject.pushKey("type");
	jsonObject.pushValue(std::string{"multi-request"});

	jsonObject.pushKey("app");
	jsonObject.startObject();

	AppIdentity appIdentity {m_requesterApplicationName, m_requesterApplicationId, ServerIdentity{m_requesterServerEndpoint.toString(), false}};
	appIdentity.toJSON(jsonObject);
	jsonObject.endObject();

	return jsonObject.dump();
}

///////////////////////////////////////////////////////////////////////////
// Responder Router

const std::string ResponderRouter::KEY = "responder-676e576d-6102-42d8-ae24-222a7000dfa0";
const std::string ResponderRouter::PORT = "port";

ResponderRouter::ResponderRouter(const std::string& name) :
	m_name{name} {

	m_impl = ImplFactory::createMultiResponderRouter();

	// Create the waiting here.
	m_waiting.reset(new Waiting{std::bind(&ResponderRouter::cancel, this)});
}

ResponderRouter::~ResponderRouter() {
	terminate();
}

void ResponderRouter::terminate() {

	if (m_impl) {
		This::getCom().removeKey(m_key);

		m_impl.reset();
	}

	setTerminated();
}

void ResponderRouter::setPollingTime(int value) {
	m_impl->setPollingTime(value);
}

void ResponderRouter::init() {

	if (isReady()) {
		// The object is already initialized.
		return;
	}

	// Set the key.
	m_key = KEY + "-" + m_name;

	// Set the dealer endpoint.
	m_dealerEndpoint = std::string{"inproc://"} + IdGenerator::newStringId();

	// Init the responder socket.
	m_impl->init(StringId::from(m_key, This::getId()), m_dealerEndpoint);

	// Store the responder data.
	json::StringObject jsonData;

	jsonData.pushKey(PORT);
	jsonData.pushValue(m_impl->getResponderPort());

	try {
		This::getCom().storeKeyValue(m_key, jsonData.dump());
	}
	catch (const KeyAlreadyExistsException& e) {
		m_impl.reset();
		throw InitException("A responder with the name \"" + m_name + "\" already exists");
	}

	setReady();
}

const std::string& ResponderRouter::getDealerEndpoint() const {
	return m_dealerEndpoint;
}

std::unique_ptr<ResponderRouter> ResponderRouter::create(const std::string& name) {
	return std::unique_ptr<ResponderRouter>(new ResponderRouter(name));
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

	json::StringObject jsonObject;

	jsonObject.pushKey("type");
	jsonObject.pushValue(std::string{"multi-responder-router"});

	jsonObject.pushKey("name");
	jsonObject.pushValue(m_name);

	jsonObject.pushKey("dealer");
	jsonObject.pushValue(m_dealerEndpoint);

	jsonObject.pushKey("app");
	jsonObject.startObject();
	AppIdentity thisIdentity {This::getName(), This::getId(), ServerIdentity{This::getServer().getEndpoint().toString(), This::getServer().usesProxy()}};
	thisIdentity.toJSON(jsonObject);
	jsonObject.endObject();

	return jsonObject.dump();
}

///////////////////////////////////////////////////////////////////////////
// Responder

Responder::Responder(const std::string& dealerEndpoint) :
	m_dealerEndpoint{dealerEndpoint} {

	m_impl = ImplFactory::createMultiResponder();

	// Create the waiting here.
	m_waiting.reset(new Waiting{std::bind(&Responder::cancel, this)});
}

Responder::~Responder() {
	terminate();
}

void Responder::terminate() {
	m_impl.reset();

	setTerminated();
}

void Responder::init() {

	if (isReady()) {
		// The object is already initialized.
		return;
	}

	// Init the responder socket.
	m_impl->init(m_dealerEndpoint);

	setReady();
}

std::unique_ptr<Responder> Responder::create(const ResponderRouter& router) {
	return std::unique_ptr<Responder>{new Responder(router.getDealerEndpoint())};
}

void Responder::cancel() {
	m_impl->cancel();
}

std::unique_ptr<Request> Responder::receive() {

	// Receive the request.
	std::unique_ptr<Request> request {m_impl->receive()};

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

	json::StringObject jsonObject;

	jsonObject.pushKey("type");
	jsonObject.pushValue(std::string{"multi-responder"});

	jsonObject.pushKey("dealer");
	jsonObject.pushValue(m_dealerEndpoint);

	jsonObject.pushKey("app");
	jsonObject.startObject();
	AppIdentity thisIdentity {This::getName(), This::getId(), ServerIdentity{This::getServer().getEndpoint().toString(), This::getServer().usesProxy()}};
	thisIdentity.toJSON(jsonObject);
	jsonObject.endObject();

	return jsonObject.dump();
}

}
}
}

std::ostream& operator<<(std::ostream& os, const cameo::coms::multi::Request& request) {

	os << request.toString();

	return os;
}

std::ostream& operator<<(std::ostream& os, const cameo::coms::multi::ResponderRouter& router) {

	os << router.toString();

	return os;
}

std::ostream& operator<<(std::ostream& os, const cameo::coms::multi::Responder& responder) {

	os << responder.toString();

	return os;
}
