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

Publisher::Publisher(const std::string& name, int numberOfSubscribers) :
	m_name(name) {

	//TODO Replace with factory.
	m_impl = std::unique_ptr<PublisherImpl>(new PublisherZmq(name, numberOfSubscribers));

	// Create the waiting here.
	m_waiting.reset(new Waiting(std::bind(&Publisher::cancelWaitForSubscribers, this)));
}

Publisher::~Publisher() {
}

void Publisher::init(const std::string& name, int numberOfSubscribers) {

	json::Object response = application::This::getCom().requestJSON(createCreatePublisherRequest(application::This::getId(), name, numberOfSubscribers));

	int publisherPort = response[message::PublisherResponse::PUBLISHER_PORT].GetInt();
	if (publisherPort == -1) {
		throw PublisherCreationException(response[message::PublisherResponse::MESSAGE].GetString());
	}
	int synchronizerPort = response[message::PublisherResponse::SYNCHRONIZER_PORT].GetInt();

	m_impl->init(publisherPort, synchronizerPort);
}

std::unique_ptr<Publisher> Publisher::create(const std::string& name, int numberOfSubscribers) {

	std::unique_ptr<Publisher> publisher = std::unique_ptr<Publisher>(new Publisher(name, numberOfSubscribers));
	publisher->init(name, numberOfSubscribers);

	return publisher;
}

const std::string& Publisher::getName() const {
	return m_name;
}

bool Publisher::waitForSubscribers() const {
	return m_impl->waitForSubscribers();
}

void Publisher::cancelWaitForSubscribers() {
	m_impl->cancelWaitForSubscribers();
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

std::string Publisher::createCreatePublisherRequest(int id, const std::string& name, int numberOfSubscribers) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushValue(message::CREATE_PUBLISHER_v0);

	request.pushKey(message::CreatePublisherRequest::ID);
	request.pushValue(id);

	request.pushKey(message::CreatePublisherRequest::NAME);
	request.pushValue(name);

	request.pushKey(message::CreatePublisherRequest::NUMBER_OF_SUBSCRIBERS);
	request.pushValue(numberOfSubscribers);

	return request.toString();
}

///////////////////////////////////////////////////////////////////////////
// Subscriber

Subscriber::Subscriber() {
	m_impl = std::unique_ptr<SubscriberImpl>(new SubscriberZmq());

	// Create the waiting here.
	m_waiting.reset(new Waiting(std::bind(&Subscriber::cancel, this)));
}

Subscriber::~Subscriber() {
}

void Subscriber::init(application::Instance & app, const std::string& publisherName) {

	m_publisherName = publisherName;
	m_appName = app.getName();
	m_appId = app.getId();
	m_appEndpoint = app.getEndpoint();

	// Get the JSON response.
	json::Object response = app.getCom().requestJSON(createConnectPublisherRequest(app.getId(), publisherName));

	int publisherPort = response[message::PublisherResponse::PUBLISHER_PORT].GetInt();
	if (publisherPort == -1) {
		throw SubscriberCreationException(response[message::PublisherResponse::MESSAGE].GetString());
	}

	int synchronizerPort = response[message::PublisherResponse::SYNCHRONIZER_PORT].GetInt();
	int numberOfSubscribers = response[message::PublisherResponse::NUMBER_OF_SUBSCRIBERS].GetInt();

	m_impl->init(m_appId, m_appEndpoint, app.getStatusEndpoint(), publisherPort, synchronizerPort, numberOfSubscribers);
}

std::unique_ptr<Subscriber> Subscriber::createSubscriber(application::Instance & app, const std::string &publisherName) {

	std::unique_ptr<Subscriber> subscriber = std::unique_ptr<Subscriber>(new Subscriber());
	subscriber->init(app, publisherName);

	return subscriber;
}

std::unique_ptr<Subscriber> Subscriber::create(application::Instance & app, const std::string& publisherName) {
	try {
		return createSubscriber(app, publisherName);

	} catch (const SubscriberCreationException& e) {
		// the publisher does not exist, so we are waiting for it
	}

	// waiting for the publisher
	application::State lastState = app.waitFor(publisherName);

	// state cannot be terminal or it means that the application has terminated that is not planned.
	if (lastState == application::SUCCESS
		|| lastState == application::STOPPED
		|| lastState == application::KILLED
		|| lastState == application::FAILURE) {
		return std::unique_ptr<Subscriber>(nullptr);
	}

	try {
		return createSubscriber(app, publisherName);

	} catch (const SubscriberCreationException& e) {
		// that should not happen
	}

	return std::unique_ptr<Subscriber>(nullptr);
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

std::string Subscriber::createConnectPublisherRequest(int id, const std::string& publisherName) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushValue(message::CONNECT_PUBLISHER_v0);

	request.pushKey(message::ConnectPublisherRequest::APPLICATION_ID);
	request.pushValue(id);

	request.pushKey(message::ConnectPublisherRequest::PUBLISHER_NAME);
	request.pushValue(publisherName);

	return request.toString();
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

