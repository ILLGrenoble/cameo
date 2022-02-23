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

#include "JSON.h"
#include "Server.h"
#include "BasicRequesterResponder.h"
#include "../factory/ImplFactory.h"
#include "../base/impl/zmq/ContextZmq.h"
#include "../base/Messages.h"
#include "../base/RequestSocket.h"
#include "../base/Waiting.h"
#include "impl/zmq/PublisherZmq.h"
#include "impl/zmq/SubscriberZmq.h"

namespace cameo {
namespace coms {

///////////////////////////////////////////////////////////////////////////////
// Publisher

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

	application::This::getCom().removeKey(m_key);
}

void Publisher::init(const std::string& name) {

	// Set the key.
	m_key = KEY + "-" + name;

	// Init the publisher and synchronizer sockets.
	m_impl->init(StringId::from(application::This::getId(), m_key));

	// Store the publisher data.
	json::StringObject jsonData;

	jsonData.pushKey(PUBLISHER_PORT);
	jsonData.pushValue(m_impl->getPublisherPort());

	jsonData.pushKey(NUMBER_OF_SUBSCRIBERS);
	jsonData.pushValue(m_numberOfSubscribers);

	try {
		application::This::getCom().storeKeyValue(m_key, jsonData.toString());
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
	catch (const coms::ResponderCreationException& e) {
		std::cerr << "Error, cannot create responder" << std::endl;
		return false;
	}
}

void Publisher::cancelWaitForSubscribers() {

	if (m_responder) {
		m_responder->cancel();
	}
}

void Publisher::sendBinary(const std::string& data) const {
	m_impl->sendBinary(data);
}

void Publisher::send(const std::string& data) const {
	m_impl->send(data);
}

void Publisher::sendTwoBinaryParts(const std::string& data1, const std::string& data2) const {
	m_impl->sendTwoBinaryParts(data1, data2);
}

bool Publisher::hasEnded() const {
	return m_impl->isEnded();
}

bool Publisher::isEnded() const {
	return m_impl->isEnded();
}

void Publisher::sendEnd() const {
	m_impl->setEnd();
}

///////////////////////////////////////////////////////////////////////////
// Subscriber

Subscriber::Subscriber() {
	m_impl = ImplFactory::createSubscriber();

	// Create the waiting here.
	m_waiting.reset(new Waiting(std::bind(&Subscriber::cancel, this)));
}

Subscriber::~Subscriber() {
}

void Subscriber::tryInit(application::Instance & app) {

	// Get the publisher data.
	try {
		std::string jsonString = app.getCom().getKeyValue(m_key);

		json::Object jsonData;
		json::parse(jsonData, jsonString);

		// Do not use publisher port but proxy port.
		//int publisherPort = publisherData[Publisher::PUBLISHER_PORT.c_str()].GetInt();
		int publisherPort = app.getCom().getPublisherProxyPort();
		int numberOfSubscribers = jsonData[Publisher::NUMBER_OF_SUBSCRIBERS.c_str()].GetInt();

		m_impl->init(m_appId, m_appEndpoint, app.getStatusEndpoint(), StringId::from(m_appId, m_key), publisherPort);

		// Synchronize the subscriber only if the number of subscribers > 0.
		if (numberOfSubscribers > 0) {
			synchronize(app);
		}
	}
	catch (...) {
		throw SubscriberCreationException("Cannot create subscriber");
	}
}

void Subscriber::synchronize(application::Instance & app) {

	try {
		std::unique_ptr<basic::Requester> requester = basic::Requester::create(app, Publisher::RESPONDER_PREFIX + m_publisherName);

		// Send a subscribe request.
		json::StringObject jsonRequest;
		jsonRequest.pushKey(message::TYPE);
		jsonRequest.pushValue(Publisher::SUBSCRIBE_PUBLISHER);

		requester->send(jsonRequest.toString());
		std::optional<std::string> response = requester->receive();
	}
	catch (const RequesterCreationException& e) {
		std::cerr << "Error, cannot create requester for subscriber" << std::endl;
	}
}

void Subscriber::init(application::Instance & app, const std::string& publisherName) {

	m_publisherName = publisherName;
	m_appName = app.getName();
	m_appId = app.getId();
	m_appEndpoint = app.getEndpoint();
	m_key = Publisher::KEY + "-" + publisherName;

	try {
		return tryInit(app);
	}
	catch (...) {
		// The publisher does not exist so we are waiting for it.
	}

	// Wait for the publisher.
	KeyValue keyValue(m_key);
	application::State lastState = app.waitFor(keyValue);

	// The state cannot be terminal or it means that the application has terminated.
	if (lastState == application::SUCCESS
		|| lastState == application::STOPPED
		|| lastState == application::KILLED
		|| lastState == application::FAILURE) {
		throw SubscriberCreationException("Cannot create subscriber");
	}

	try {
		tryInit(app);
	}
	catch (...) {
		// Should not happen.
	}
}

std::unique_ptr<Subscriber> Subscriber::create(application::Instance & app, const std::string &publisherName) {

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
	return m_impl->isEnded();
}

bool Subscriber::isEnded() const {
	return m_impl->isEnded();
}

bool Subscriber::isCanceled() const {
	return m_impl->isCanceled();
}

std::optional<std::string> Subscriber::receiveBinary() const {
	return m_impl->receiveBinary();
}

std::optional<std::string> Subscriber::receive() const {
	return m_impl->receive();
}

std::optional<std::tuple<std::string, std::string>> Subscriber::receiveTwoBinaryParts() const {
	return m_impl->receiveTwoBinaryParts();
}

void Subscriber::cancel() {
	m_impl->cancel();
}

std::ostream& operator<<(std::ostream& os, const cameo::coms::Publisher& publisher) {

	os << "pub." << publisher.getName()
		<< ":" << application::This::getName()
		<< "." << application::This::getId()
		<< "@" << application::This::getEndpoint().toString();

	return os;
}

std::ostream& operator<<(std::ostream& os, const cameo::coms::Subscriber& subscriber) {

	os << "sub." << subscriber.getPublisherName()
		<< ":" << subscriber.getAppName()
		<< "." << subscriber.getAppId()
		<< "@" << subscriber.getAppEndpoint();

	return os;
}

}
}

