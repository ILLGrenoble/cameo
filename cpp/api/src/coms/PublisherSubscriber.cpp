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

///////////////////////////////////////////////////////////////////////////////
// Publisher

PublisherCreationException::PublisherCreationException(const std::string& message) :
	RemoteException(message) {
}


const std::string Publisher::KEY = "publisher-55845880-56e9-4ad6-bea1-e84395c90b32";
const std::string Publisher::PUBLISHER_PORT = "publisher_port";
const std::string Publisher::RESPONDER_PREFIX = "publisher:";
const std::string Publisher::NUMBER_OF_SUBSCRIBERS = "n_subscribers";

Publisher::Publisher(const std::string& name, int numberOfSubscribers) :
	m_name(name), m_numberOfSubscribers(numberOfSubscribers) {

	m_impl = ImplFactory::createPublisher();

	// Create the waiting here.
	m_waiting.reset(new Waiting(std::bind(&Publisher::cancelWaitForSubscribers, this)));
}

Publisher::~Publisher() {
	terminate();
}

void Publisher::terminate() {

	if (m_impl) {
		This::getCom().removeKey(m_key);

		m_responder.reset();
		m_impl.reset();
	}
}

void Publisher::init(const std::string& name) {

	// Set the key.
	m_key = KEY + "-" + name;

	// Init the publisher and synchronizer sockets.
	m_impl->init(StringId::from(This::getId(), m_key));

	// Store the publisher data.
	json::StringObject jsonData;

	jsonData.pushKey(PUBLISHER_PORT);
	jsonData.pushValue(m_impl->getPublisherPort());

	jsonData.pushKey(NUMBER_OF_SUBSCRIBERS);
	jsonData.pushValue(m_numberOfSubscribers);

	try {
		This::getCom().storeKeyValue(m_key, jsonData.dump());
	}
	catch (const KeyAlreadyExistsException& e) {
		throw PublisherCreationException("A publisher with the name \"" + name + "\" already exists");
	}
}

std::unique_ptr<Publisher> Publisher::create(const std::string& name, int numberOfSubscribers) {

	std::unique_ptr<Publisher> publisher = std::unique_ptr<Publisher>(new Publisher(name, numberOfSubscribers));
	publisher->init(name);

	return publisher;
}

const std::string& Publisher::getName() const {
	return m_name;
}

bool Publisher::waitForSubscribers() {

	try {
		m_responder = coms::basic::Responder::create(RESPONDER_PREFIX + m_name);
		m_responder->init();

		// Loop until the number of subscribers is reached.
		int counter = 0;

		while (counter < m_numberOfSubscribers) {

			std::unique_ptr<basic::Request> request = m_responder->receive();

			if (!request) {
				return false;
			}

			// Get the JSON request.
			json::Object jsonRequest;
			json::parse(jsonRequest, request->get());

			int type = jsonRequest[message::TYPE].GetInt();

			if (type == SUBSCRIBE_PUBLISHER) {
				counter++;
			}

			request->reply("OK");
		}

		bool canceled = m_responder->isCanceled();
		m_responder.reset();

		return !canceled;
	}
	catch (const ResponderCreationException& e) {
		return false;
	}
}

void Publisher::cancelWaitForSubscribers() {

	if (m_responder) {
		m_responder->cancel();
	}
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

	return std::string("pub.") + getName()
		+ ":" + This::getName()
		+ "." + std::to_string(This::getId())
		+ "@" + This::getEndpoint().toString();

}

///////////////////////////////////////////////////////////////////////////
// Subscriber

SubscriberCreationException::SubscriberCreationException(const std::string& message) :
	RemoteException(message) {
}


Subscriber::Subscriber() :
	m_useProxy(false) {

	m_impl = ImplFactory::createSubscriber();

	// Create the waiting here.
	m_waiting.reset(new Waiting(std::bind(&Subscriber::cancel, this)));
}

Subscriber::~Subscriber() {
	terminate();
}

void Subscriber::terminate() {
	m_impl.reset();
}

void Subscriber::synchronize(const App & app) {

	std::unique_ptr<Requester> requester = Requester::create(app, Publisher::RESPONDER_PREFIX + m_publisherName);
	requester->init();

	// Send a subscribe request.
	json::StringObject jsonRequest;
	jsonRequest.pushKey(message::TYPE);
	jsonRequest.pushValue(Publisher::SUBSCRIBE_PUBLISHER);

	requester->send(jsonRequest.dump());
	std::optional<std::string> response = requester->receive();
}

void Subscriber::init(const App & app, const std::string& publisherName) {

	m_publisherName = publisherName;
	m_appName = app.getName();
	m_appId = app.getId();
	m_appEndpoint = app.getEndpoint();
	m_key = Publisher::KEY + "-" + publisherName;
	m_useProxy = app.usesProxy();

	// Get the publisher data.
	try {
		std::string jsonString = app.getCom().getKeyValueGetter(m_key)->get();

		json::Object jsonData;
		json::parse(jsonData, jsonString);

		int numberOfSubscribers = jsonData[Publisher::NUMBER_OF_SUBSCRIBERS.c_str()].GetInt();

		Endpoint endpoint;

		// The endpoint depends on the use of the proxy.
		if (m_useProxy) {
			int publisherPort = app.getCom().getPublisherProxyPort();
			endpoint = app.getEndpoint().withPort(publisherPort);
		}
		else {
			int publisherPort = jsonData[Publisher::PUBLISHER_PORT.c_str()].GetInt();
			endpoint = app.getEndpoint().withPort(publisherPort);
		}

		m_impl->init(m_appId, endpoint, app.getStatusEndpoint(), StringId::from(m_appId, m_key));

		// Synchronize the subscriber only if the number of subscribers > 0.
		if (numberOfSubscribers > 0) {
			synchronize(app);
		}
	}
	catch (...) {
		throw SubscriberCreationException("Cannot create subscriber");
	}
}

std::unique_ptr<Subscriber> Subscriber::create(App & app, const std::string &publisherName) {

	std::unique_ptr<Subscriber> subscriber = std::unique_ptr<Subscriber>(new Subscriber());
	subscriber->init(app, publisherName);

	return subscriber;
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
	m_impl->cancel();
}

std::string Subscriber::toString() const {

	return std::string("sub.") + getPublisherName()
		+ ":" + getAppName()
		+ "." + std::to_string(getAppId())
		+ "@" + getAppEndpoint().toString();

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

