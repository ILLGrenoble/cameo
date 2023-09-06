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

#include "PublisherSubscriber.h"

#include "ImplFactory.h"
#include "RequestSocket.h"
#include "Messages.h"
#include "Server.h"
#include "ContextZmq.h"
#include "Waiting.h"
#include "BasicResponder.h"
#include "Requester.h"
#include "impl/zmq/PublisherZmq.h"
#include "impl/zmq/SubscriberZmq.h"

namespace cameo {
namespace coms {

constexpr int SYNC_TIMEOUT = 100;

///////////////////////////////////////////////////////////////////////////////
// Publisher

const std::string Publisher::KEY = "publisher-55845880-56e9-4ad6-bea1-e84395c90b32";
const std::string Publisher::PUBLISHER_PORT = "publisher_port";
const std::string Publisher::RESPONDER_PREFIX = "publisher:";
const std::string Publisher::NUMBER_OF_SUBSCRIBERS = "n_subscribers";
const std::string Publisher::SYNC_SUBSCRIBERS = "sync_subscribers";

Publisher::Publisher(const std::string& name, int numberOfSubscribers, bool syncSubscribers) :
	m_name{name},
	m_numberOfSubscribers{numberOfSubscribers},
	m_syncSubscribers{syncSubscribers},
	m_canceled{false} {

	m_impl = ImplFactory::createPublisher(syncSubscribers);

	// Create the waiting here.
	m_waiting.reset(new Waiting{std::bind(&Publisher::cancel, this)});
}

Publisher::~Publisher() {
	terminate();
}

void Publisher::terminate() {

	if (m_impl) {
		This::getCom().removeKey(m_key);

		if (m_responderThread) {
			m_responder->cancel();
			m_responderThread->join();
			m_responder.reset();
		}

		m_impl.reset();
	}

	setTerminated();
}

void Publisher::init() {

	if (isReady()) {
		// The object is already initialized.
		return;
	}

	// Set the key.
	m_key = KEY + "-" + m_name;

	// Init the publisher and synchronizer sockets.
	m_impl->init(StringId::from(m_key, This::getId()));

	// Store the publisher data.
	json::StringObject jsonData;

	jsonData.pushKey(PUBLISHER_PORT);
	jsonData.pushValue(m_impl->getPublisherPort());

	jsonData.pushKey(NUMBER_OF_SUBSCRIBERS);
	jsonData.pushValue(m_numberOfSubscribers);

	jsonData.pushKey(SYNC_SUBSCRIBERS);
	jsonData.pushValue(m_syncSubscribers);

	try {
		This::getCom().storeKeyValue(m_key, jsonData.dump());
	}
	catch (const KeyAlreadyExistsException& e) {
		m_impl.reset();
		throw InitException("A publisher with the name \"" + m_name + "\" already exists");
	}

	// Create the responder thread if subscribers are synchronized or waiting for subscribers is enabled.
	if (m_numberOfSubscribers > 0 || m_syncSubscribers) {

		try {
			m_responder = coms::basic::Responder::create(RESPONDER_PREFIX + m_name);
			m_responder->init();

			m_responderThread = std::make_unique<std::thread>(std::bind(&Publisher::responderLoop, this));;
		}
		catch (const InitException& e) {
			return;
		}

		// Wait for the subscribers.
		if (m_numberOfSubscribers > 0) {
			if (!waitForSubscribers()) {
				return;
			}
		}
	}

	setReady();
}

std::unique_ptr<Publisher> Publisher::create(const std::string& name, int numberOfSubscribers, bool syncSubscribers) {
	return std::unique_ptr<Publisher>{new Publisher(name, numberOfSubscribers, syncSubscribers)};
}

const std::string& Publisher::getName() const {
	return m_name;
}

void Publisher::responderLoop() {

	while (true) {

		std::unique_ptr<basic::Request> request {m_responder->receive()};

		if (!request) {
			return;
		}

		// Get the JSON request.
		json::Object jsonRequest;
		json::parse(jsonRequest, request->get());

		int type {jsonRequest[message::TYPE].GetInt()};

		if (type == SUBSCRIBE_PUBLISHER) {
			std::unique_ptr<int> item { new int{SUBSCRIBE_PUBLISHER} };
			m_responderQueue.push(item);
		}
		else if (type == message::SYNC_STREAM) {
			m_impl->sendSync();
		}

		request->reply("OK");
	}
}

bool Publisher::waitForSubscribers() {

	// Loop until the number of subscribers is reached.
	int counter = 0;

	while (counter < m_numberOfSubscribers) {

		std::unique_ptr<int> item = m_responderQueue.pop();

		if (*item.get() == SUBSCRIBE_PUBLISHER) {
			counter++;
		}
		else if (*item.get() == CANCEL_RESPONDER) {
			return false;
		}
	}

	return true;
}

void Publisher::cancel() {

	if (m_responder) {
		m_canceled = true;
		m_responder->cancel();

		std::unique_ptr<int> item { new int{CANCEL_RESPONDER} };
		m_responderQueue.push(item);
	}
}

bool Publisher::isCanceled() const {
	return m_canceled;
}

void Publisher::send(const std::string& data) const {
	m_impl->send(data);
}

void Publisher::sendTwoParts(const std::string& data1, const std::string& data2) const {
	m_impl->sendTwoParts(data1, data2);
}

bool Publisher::hasEnded() const {
	return m_impl->hasEnded();
}

void Publisher::sendEnd() const {
	m_impl->setEnd();
}

std::string Publisher::toString() const {

	json::StringObject jsonObject;

	jsonObject.pushKey("type");
	jsonObject.pushValue(std::string{"publisher"});

	jsonObject.pushKey("name");
	jsonObject.pushValue(m_name);

	jsonObject.pushKey("app");
	jsonObject.startObject();
	AppIdentity thisIdentity {This::getName(), This::getId(), ServerIdentity{This::getServer().getEndpoint().toString(), This::getServer().usesProxy()}};
	thisIdentity.toJSON(jsonObject);
	jsonObject.endObject();

	return jsonObject.dump();
}

///////////////////////////////////////////////////////////////////////////
// Subscriber

Subscriber::Subscriber(App & app, const std::string &publisherName, bool checkApp) :
	m_app{app},
	m_publisherName{publisherName},
	m_checkApp{checkApp},
	m_timeout{-1},
	m_useProxy{m_app.usesProxy()},
	m_appName{m_app.getName()},
	m_appId{m_app.getId()},
	m_appEndpoint{m_app.getEndpoint()},
	m_impl{ImplFactory::createSubscriber()},
	m_waiting{new Waiting{std::bind(&Subscriber::cancel, this)}},
	m_key{Publisher::KEY + "-" + m_publisherName},
	m_keyValueGetter{m_app.getCom().createKeyValueGetter(m_key)} {
}

Subscriber::~Subscriber() {
	terminate();
}

void Subscriber::terminate() {

	if (m_requester) {
		m_requester->terminate();
		m_requester.reset();
	}

	m_impl.reset();
	setTerminated();
}

void Subscriber::setTimeout(int value) {
	m_timeout = value;
}

int Subscriber::getTimeout() const {
	return m_timeout;
}

void Subscriber::synchronize(const TimeoutCounter& timeout, int numberOfSubscribers, bool syncSubscribers) {

	// Create the requester.
	m_requester = Requester::create(m_app, Publisher::RESPONDER_PREFIX + m_publisherName);
	if (m_checkApp) {
		m_requester->setCheckApp();
	}

	// Set the timeout that can be -1.
	m_requester->setTimeout(timeout.remains());

	// A Timeout exception may be thrown.
	m_requester->init();

	// Set the timeout again because init() may have taken time.
	m_requester->setTimeout(timeout.remains());

	// Check timeout.
	bool timedOut {false};

	// Check sync subscribers.
	if (syncSubscribers) {

		int syncTimeout = 0;

		while (!timedOut) {
			// Send a sync request.
			json::StringObject jsonRequest;
			jsonRequest.pushKey(message::TYPE);
			jsonRequest.pushValue(message::SYNC_STREAM);

			m_requester->send(jsonRequest.dump());
			std::optional<std::string> response {m_requester->receive()};

			syncTimeout += SYNC_TIMEOUT;

			// Check subscriber.
			if (m_impl->sync(syncTimeout)) {
				break;
			}

			// Check timeout.
			timedOut = m_requester->hasTimedout();
		}
	}

	// Send subscription.
	if (!timedOut) {

		// Check number of subscribers.
		if (numberOfSubscribers > 0) {
			// Send a subscribe request.
			json::StringObject jsonRequest;
			jsonRequest.pushKey(message::TYPE);
			jsonRequest.pushValue(Publisher::SUBSCRIBE_PUBLISHER);

			m_requester->send(jsonRequest.dump());
			std::optional<std::string> response {m_requester->receive()};
		}
	}

	// Check timeout.
	timedOut = m_requester->hasTimedout();

	// Reset the requester as it is not used any more.
	m_requester.reset();

	if (timedOut) {
		throw Timeout();
	}
}

void Subscriber::init() {

	if (isReady()) {
		// The object is already initialized.
		return;
	}

	// Get the publisher data.
	try {
		TimeoutCounter timeoutCounter {m_timeout};

		std::string jsonString {m_keyValueGetter->get(timeoutCounter)};

		if (m_keyValueGetter->isCanceled()) {
			return;
		}

		json::Object jsonData;
		json::parse(jsonData, jsonString);

		int numberOfSubscribers {jsonData[Publisher::NUMBER_OF_SUBSCRIBERS.c_str()].GetInt()};
		bool syncSubscribers {jsonData[Publisher::SYNC_SUBSCRIBERS.c_str()].GetBool()};

		Endpoint endpoint;

		// The endpoint depends on the use of the proxy.
		if (m_useProxy) {
			int publisherPort {m_app.getCom().getPublisherProxyPort()};
			endpoint = m_app.getEndpoint().withPort(publisherPort);
		}
		else {
			int publisherPort {jsonData[Publisher::PUBLISHER_PORT.c_str()].GetInt()};
			endpoint = m_app.getEndpoint().withPort(publisherPort);
		}

		m_impl->init(m_appId, endpoint, m_app.getStatusEndpoint(), StringId::from(m_key, m_appId), m_checkApp);

		// Synchronize the subscriber only if the number of subscribers > 0.
		if (numberOfSubscribers > 0 || syncSubscribers) {
			synchronize(timeoutCounter, numberOfSubscribers, syncSubscribers);
		}
	}
	catch (const ConnectionTimeout&) {
		throw;
	}
	catch (const Timeout&) {
		throw SynchronizationTimeout(std::string{"Subscriber cannot synchronize publisher '"} + m_publisherName + "'");
	}
	catch (const SynchronizationTimeout&) {
		throw SynchronizationTimeout(std::string{"Subscriber cannot synchronize publisher '"} + m_publisherName + "'");
	}
	catch (const InitException& e) {
		throw InitException(std::string{"Cannot initialize subscriber to publisher '"} + m_publisherName + "': Cannot initialize internal requester");
	}
	catch (const std::exception& e) {
		throw InitException(std::string{"Cannot initialize subscriber to publisher '"} + m_publisherName + "':" + e.what());
	}

	setReady();
}

std::unique_ptr<Subscriber> Subscriber::create(App & app, const std::string &publisherName, bool checkApp) {
	return std::unique_ptr<Subscriber>{new Subscriber(app, publisherName, checkApp)};
}

const std::string& Subscriber::getPublisherName() const {
	return m_publisherName;
}

const std::string& Subscriber::getAppName() const {
	return m_appName;
}

int Subscriber::getAppId() const {
	return m_appId;
}

Endpoint Subscriber::getAppEndpoint() const {
	return m_appEndpoint;
}

bool Subscriber::hasEnded() const {
	return m_impl->hasEnded();
}

bool Subscriber::isCanceled() const {
	return m_impl->isCanceled();
}

std::optional<std::string> Subscriber::receive() const {
	return m_impl->receive();
}

std::optional<std::tuple<std::string, std::string>> Subscriber::receiveTwoParts() const {
	return m_impl->receiveTwoParts();
}

void Subscriber::cancel() {
	m_keyValueGetter->cancel();

	if (m_requester) {
		m_requester->cancel();
	}
	m_impl->cancel();
}

std::string Subscriber::toString() const {

	json::StringObject jsonObject;

	jsonObject.pushKey("type");
	jsonObject.pushValue(std::string{"subscriber"});

	jsonObject.pushKey("name");
	jsonObject.pushValue(m_publisherName);

	jsonObject.pushKey("app");
	jsonObject.startObject();

	AppIdentity appIdentity {m_appName, m_appId, ServerIdentity{m_appEndpoint.toString(), m_useProxy}};
	appIdentity.toJSON(jsonObject);
	jsonObject.endObject();

	return jsonObject.dump();
}

std::ostream& operator<<(std::ostream& os, const cameo::coms::Publisher& publisher) {

	os << publisher.toString();

	return os;
}

std::ostream& operator<<(std::ostream& os, const cameo::coms::Subscriber& subscriber) {

	os << subscriber.toString();

	return os;
}

}
}

