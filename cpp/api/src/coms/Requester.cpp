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

#include "Requester.h"
#include "BasicResponder.h"
#include "ImplFactory.h"
#include "RequestSocket.h"
#include "Messages.h"
#include "Server.h"
#include "ContextZmq.h"
#include "Waiting.h"
#include "impl/zmq/RequesterZmq.h"
#include "impl/zmq/BasicResponderZmq.h"

namespace cameo {
namespace coms {

///////////////////////////////////////////////////////////////////////////
// Requester

Requester::Requester(const App & app, const std::string &responderName) :
	m_app{app},
	m_responderName{responderName},
	m_timeout{-1},
	m_useProxy{false},
	m_appId{0} {

	m_impl = ImplFactory::createRequester();

	// Create the waiting here.
	m_waiting.reset(new Waiting{std::bind(&Requester::cancel, this)});
}

Requester::~Requester() {
	terminate();
}

void Requester::terminate() {
	m_impl.reset();
}

void Requester::init() {

	m_appName = m_app.getName();
	m_appId = m_app.getId();
	m_appEndpoint = m_app.getEndpoint();
	m_key = basic::Responder::KEY + "-" + m_responderName;
	m_useProxy = m_app.usesProxy();

	// Get the responder data.
	try {
		TimeoutCounter timeoutCounter {m_timeout};

		std::string jsonString {m_app.getCom().getKeyValueGetter(m_key)->get(timeoutCounter)};

		json::Object jsonData;
		json::parse(jsonData, jsonString);

		Endpoint endpoint;

		// The endpoint depends on the use of the proxy.
		if (m_useProxy) {
			int responderPort {m_app.getCom().getResponderProxyPort()};
			endpoint = m_app.getEndpoint().withPort(responderPort);
		}
		else {
			int responderPort {jsonData[basic::Responder::PORT.c_str()].GetInt()};
			endpoint = m_app.getEndpoint().withPort(responderPort);
		}

		m_impl->init(endpoint, StringId::from(m_key, m_appId), timeoutCounter);
	}
	catch (const std::exception& e) {
		throw InitException(std::string("Cannot initialize requester: ") + e.what());
	}
}

std::unique_ptr<Requester> Requester::create(const App & app, const std::string& responderName) {
	return std::unique_ptr<Requester>{new Requester(app, responderName)};
}

void Requester::setTimeout(int value) {
	m_timeout = value;
	m_impl->setTimeout(value);
}

int Requester::getTimeout() const {
	return m_timeout;
}

void Requester::setPollingTime(int value) {
	m_impl->setPollingTime(value);
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

void Requester::send(const std::string& request) {
	m_impl->send(request);
}

void Requester::sendTwoParts(const std::string& request1, const std::string& request2) {
	m_impl->sendTwoParts(request1, request2);
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

std::string Requester::toString() const {

	json::StringObject jsonObject;

	jsonObject.pushKey("type");
	jsonObject.pushValue(std::string{"requester"});

	jsonObject.pushKey("responder");
	jsonObject.pushValue(m_responderName);

	jsonObject.pushKey("app");
	jsonObject.startObject();

	AppIdentity appIdentity {m_appName, m_appId, ServerIdentity{m_appEndpoint.toString(), m_useProxy}};
	appIdentity.toJSON(jsonObject);
	jsonObject.endObject();

	return jsonObject.dump();
}

std::ostream& operator<<(std::ostream& os, const Requester& requester) {

	os << requester.toString();

	return os;
}

}
}

