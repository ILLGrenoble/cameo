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

Requester::Requester() :
	m_useProxy(false),
	m_appId(0) {

	m_impl = ImplFactory::createBasicRequester();

	// Create the waiting here.
	m_waiting.reset(new Waiting(std::bind(&Requester::cancel, this)));
}

Requester::~Requester() {
	terminate();
}

void Requester::terminate() {
	m_impl.reset();
}

void Requester::tryInit(application::Instance & app) {

	// Memorizes proxy.
	m_useProxy = app.usesProxy();

	// Get the responder data.
	try {
		std::string jsonString = app.getCom().getKeyValue(m_key);

		json::Object jsonData;
		json::parse(jsonData, jsonString);

		Endpoint endpoint;

		// The endpoint depends on the use of the proxy.
		if (m_useProxy) {
			int responderPort = app.getCom().getResponderProxyPort();
			endpoint = app.getEndpoint().withPort(responderPort);
		}
		else {
			int responderPort = jsonData[basic::Responder::PORT.c_str()].GetInt();
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
	m_key = basic::Responder::KEY + "-" + responderName;

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

std::ostream& operator<<(std::ostream& os, const Requester& requester) {

	os << "req." << requester.m_responderName
		<< ":" << requester.m_appName
		<< "." << requester.m_appId
		<< "@" << requester.m_appEndpoint;

	return os;
}

}
}

