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

#include "../base/impl/ContextImpl.h"
#include "../base/impl/RequestSocketImpl.h"
#include "../base/Messages.h"
#include "impl/PublisherImpl.h"
#include "impl/SubscriberImpl.h"
#include "JSON.h"
#include "Server.h"

namespace cameo {
namespace coms {

///////////////////////////////////////////////////////////////////////////////
// Publisher

Publisher::Publisher(int publisherPort, int synchronizerPort, const std::string& name, int numberOfSubscribers) :
	m_impl(new PublisherImpl(publisherPort, synchronizerPort, name, numberOfSubscribers)) {

	// Create the waiting here.
	m_waiting.reset(m_impl->waiting());
}

Publisher::~Publisher() {
}

std::unique_ptr<Publisher> Publisher::create(const std::string& name, int numberOfSubscribers) {

	json::Object response = application::This::getCom().requestJSON(createCreatePublisherRequest(application::This::getId(), name, numberOfSubscribers));

	int publisherPort = response[message::PublisherResponse::PUBLISHER_PORT].GetInt();
	if (publisherPort == -1) {
		throw PublisherCreationException(response[message::PublisherResponse::MESSAGE].GetString());
	}
	int synchronizerPort = response[message::PublisherResponse::SYNCHRONIZER_PORT].GetInt();

	return std::unique_ptr<Publisher>(new Publisher(publisherPort, synchronizerPort, name, numberOfSubscribers));
}


const std::string& Publisher::getName() const {
	return m_impl->getName();
}

const std::string& Publisher::getApplicationName() const {
	return m_impl->getApplicationName();
}

int Publisher::getApplicationId() const {
	return m_impl->getApplicationId();
}

std::string Publisher::getApplicationEndpoint() const {
	return m_impl->getApplicationEndpoint();
}

bool Publisher::waitForSubscribers() const {
	return m_impl->waitForSubscribers();
}

void Publisher::cancelWaitForSubscribers() {
	m_waiting->cancel();
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
	request.pushInt(message::CREATE_PUBLISHER_v0);

	request.pushKey(message::CreatePublisherRequest::ID);
	request.pushInt(id);

	request.pushKey(message::CreatePublisherRequest::NAME);
	request.pushString(name);

	request.pushKey(message::CreatePublisherRequest::NUMBER_OF_SUBSCRIBERS);
	request.pushInt(numberOfSubscribers);

	return request.toString();
}


///////////////////////////////////////////////////////////////////////////
// Subscriber

Subscriber::Subscriber(int publisherPort, int synchronizerPort, const std::string & publisherName, int numberOfSubscribers, application::Instance &instance) :
	m_impl(new SubscriberImpl(publisherPort, synchronizerPort, publisherName, numberOfSubscribers, instance)) {
}

Subscriber::~Subscriber() {
}

std::unique_ptr<Subscriber> Subscriber::createSubscriber(application::Instance & instance, const std::string& publisherName, const std::string& instanceName) {

	// Get the JSON response.
	json::Object response = instance.getCom().requestJSON(createConnectPublisherRequest(instance.getId(), publisherName));

	int publisherPort = response[message::PublisherResponse::PUBLISHER_PORT].GetInt();
	if (publisherPort == -1) {
		throw SubscriberCreationException(response[message::PublisherResponse::MESSAGE].GetString());
	}

	int synchronizerPort = response[message::PublisherResponse::SYNCHRONIZER_PORT].GetInt();
	int numberOfSubscribers = response[message::PublisherResponse::NUMBER_OF_SUBSCRIBERS].GetInt();

	// TODO simplify the use of some variables: e.g. m_serverEndpoint accessible from this.
	std::unique_ptr<Subscriber> subscriber(new Subscriber(publisherPort, synchronizerPort, publisherName, numberOfSubscribers, instance));
	subscriber->init();

	return subscriber;
}

std::unique_ptr<Subscriber> Subscriber::create(application::Instance & instance, const std::string& publisherName) {
	try {
		return createSubscriber(instance, publisherName, instance.getName());

	} catch (const SubscriberCreationException& e) {
		// the publisher does not exist, so we are waiting for it
	}

	// waiting for the publisher
	application::State lastState = instance.waitFor(publisherName);

	// state cannot be terminal or it means that the application has terminated that is not planned.
	if (lastState == application::SUCCESS
		|| lastState == application::STOPPED
		|| lastState == application::KILLED
		|| lastState == application::FAILURE) {
		return std::unique_ptr<Subscriber>(nullptr);
	}

	try {
		return createSubscriber(instance, publisherName, instance.getName());

	} catch (const SubscriberCreationException& e) {
		// that should not happen
	}

	return std::unique_ptr<Subscriber>(nullptr);
}

void Subscriber::init() {
	m_impl->init();

	// Create the waiting here.
	m_waiting.reset(m_impl->waiting());
}

const std::string& Subscriber::getPublisherName() const {
	return m_impl->m_publisherName;
}

const std::string& Subscriber::getInstanceName() const {
	return m_impl->m_instanceName;
}

int Subscriber::getInstanceId() const {
	return m_impl->m_instanceId;
}

std::string Subscriber::getInstanceEndpoint() const {
	return m_impl->m_serverEndpoint.toString();
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
	m_waiting->cancel();
}

std::string Subscriber::createConnectPublisherRequest(int id, const std::string& publisherName) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushInt(message::CONNECT_PUBLISHER_v0);

	request.pushKey(message::ConnectPublisherRequest::APPLICATION_ID);
	request.pushInt(id);

	request.pushKey(message::ConnectPublisherRequest::PUBLISHER_NAME);
	request.pushString(publisherName);

	return request.toString();
}

std::ostream& operator<<(std::ostream& os, const cameo::coms::Publisher& publisher) {

	os << "pub." << publisher.getName()
		<< ":" << publisher.getApplicationName()
		<< "." << publisher.getApplicationId()
		<< "@" << publisher.getApplicationEndpoint();

	return os;
}

std::ostream& operator<<(std::ostream& os, const cameo::coms::Subscriber& subscriber) {

	os << "sub." << subscriber.getPublisherName()
		<< ":" << subscriber.getInstanceName()
		<< "." << subscriber.getInstanceId()
		<< "@" << subscriber.getInstanceEndpoint();

	return os;
}

}
}

