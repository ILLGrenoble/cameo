/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
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

Requester::Checker::Checker(Requester &requester) :
	m_requester{requester} {

	// Connect the app to have an instance that is accessed only by the Checker thread.
	m_app = m_requester.m_app.connect();
}

void Requester::Checker::start() {

	// Start the thread.
	m_thread = std::make_unique<std::thread>([&] {

		// Wait for the app that can be canceled.
		state::Value state = m_app->waitFor();
		if (state == state::FAILURE) {
			// Cancel the requester if the app fails.
			m_requester.cancel();
		}
	});
}

void Requester::Checker::terminate() {

	// Cancel the waitFor() call.
	m_app->cancel();

	// Clean the thread.
	if (m_thread) {
		m_thread->join();
	}
}

Requester::Requester(const App & app, const std::string &responderName) :
	m_app{app},
	m_responderName{responderName},
	m_checkApp{false},
	m_timeout{-1},
	m_useProxy{m_app.usesProxy()},
	m_appName{m_app.getName()},
	m_appId{m_app.getId()},
	m_appEndpoint{m_app.getEndpoint()},
	m_impl{ImplFactory::createRequester()},
	m_waiting{new Waiting{std::bind(&Requester::cancel, this)}},
	m_key{basic::Responder::KEY + "-" + m_responderName},
	m_keyValueGetter{m_app.getCom().createKeyValueGetter(m_key)} {
}

Requester::~Requester() {
	terminate();
}

void Requester::terminate() {

	Pingable::terminate();

	if (m_checker) {
		m_checker->terminate();
		m_checker.reset();
	}

	m_impl.reset();
	setTerminated();
}

void Requester::init() {

	if (isReady() || isCanceled()) {
		// The object is already initialized.
		return;
	}

	// Get the responder data.
	try {
		TimeoutCounter timeoutCounter {m_timeout};

		std::string jsonString {m_keyValueGetter->get(timeoutCounter)};

		if (m_keyValueGetter->isCanceled()) {
			return;
		}

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
	catch (const ConnectionTimeout&) {
		throw;
	}
	catch (const Timeout&) {
		throw SynchronizationTimeout(std::string{"Requester cannot synchronize responder '"} + m_responderName + "'");
	}
	catch (const std::exception& e) {
		throw InitException(std::string{"Cannot initialize requester to responder '"} + m_responderName + "': " + e.what());
	}

	// Start the checker if it was created.
	if (m_checkApp) {
		// The creation of the Checker object can throw a ConnectionTimeout exception.
		m_checker = std::make_unique<Checker>(*this);
		m_checker->start();
	}

	setReady();

	Pingable::init();
}

std::unique_ptr<Requester> Requester::create(const App & app, const std::string& responderName) {
	return std::unique_ptr<Requester>{new Requester(app, responderName)};
}

void Requester::setCheckApp(bool value) {
	m_checkApp = value;
}

void Requester::setTimeout(int value) {
	std::unique_lock<std::mutex> lock(m_mutex);
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

void Requester::startRequest() {
	m_mutex.lock();
}

void Requester::endRequest() {
	m_mutex.unlock();
}

std::optional<std::string> Requester::request(const std::string& request) {
	std::unique_lock<std::mutex> lock(m_mutex);
	m_impl->send(request);
	return m_impl->receive();
}

std::optional<std::string> Requester::request(const std::string& requestPart1, const std::string& requestPart2) {
	std::unique_lock<std::mutex> lock(m_mutex);
	m_impl->sendTwoParts(requestPart1, requestPart2);
	return m_impl->receive();
}

void Requester::cancel() {
	m_keyValueGetter->cancel();
	m_impl->cancel();
}

bool Requester::isCanceled() const {
	return m_impl->isCanceled();
}

bool Requester::hasTimedout() const {
	return m_impl->hasTimedout();
}

bool Requester::ping(int timeout) {

	if (!isReady()) {
		return false;
	}

	// Lock only if the mutex is available.
	std::unique_lock<std::mutex> lock(m_mutex, std::defer_lock);
	if (lock.try_lock()) {
		int pingTimeout = 0;
		if (m_timeout == -1) {
			pingTimeout = timeout * 1000;
			m_impl->setTimeout(pingTimeout);
		}

		m_impl->ping();
		std::optional<std::string> response = m_impl->receive();

		if (m_timeout == -1) {
			m_impl->setTimeout(0);
		}

		if (response.has_value()) {
			return true;
		}
	}

	return false;
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

}
}

std::ostream& operator<<(std::ostream& os, const cameo::coms::Requester& requester) {

	os << requester.toString();

	return os;
}
