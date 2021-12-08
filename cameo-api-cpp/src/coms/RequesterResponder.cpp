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

#include "RequesterResponder.h"

#include "../base/impl/ContextImpl.h"
#include "../base/impl/RequestSocketImpl.h"
#include "../base/Messages.h"
#include "impl/RequesterImpl.h"
#include "impl/RequestImpl.h"
#include "impl/ResponderImpl.h"
#include "JSON.h"
#include "Server.h"

namespace cameo {
namespace coms {

///////////////////////////////////////////////////////////////////////////
// Request

Request::Request(std::unique_ptr<RequestImpl> & impl) :
	m_impl(std::move(impl)) {
}

Request::~Request() {
}

std::string Request::getObjectId() const {

	// Local id is missing.
	return "request:"
		+ m_impl->m_requesterApplicationName
		+ "."
		+ std::to_string(m_impl->m_requesterApplicationId)
		+ "@"
		+ m_impl->m_requesterServerEndpoint;
}

std::string Request::getRequesterEndpoint() const {
	return m_impl->m_requesterServerEndpoint;
}

void Request::setTimeout(int value) {
	m_impl->setTimeout(value);
}

const std::string& Request::getBinary() const {
	return m_impl->m_message;
}

std::string Request::get() const {

	std::string data;
	parse(m_impl->m_message, data);

	return data;
}

const std::string& Request::getSecondBinaryPart() const {
	return m_impl->m_message2;
}

bool Request::replyBinary(const std::string& response) {
	return m_impl->replyBinary(response);
}

bool Request::reply(const std::string& response) {
	return m_impl->reply(response);
}

std::unique_ptr<application::Instance> Request::connectToRequester() {

	// Instantiate the requester server if it does not exist.
	if (m_requesterServer.get() == nullptr) {
		m_requesterServer.reset(new Server(m_impl->m_requesterServerEndpoint, m_impl->m_timeout));
	}

	// Connect and find the instance.
	application::InstanceArray instances = m_requesterServer->connectAll(m_impl->m_requesterApplicationName);

	for (int i = 0; i < instances.size(); i++) {
		if (instances[i]->getId() == m_impl->m_requesterApplicationId) {
			return std::unique_ptr<application::Instance>(std::move(instances[i]));
		}
	}

	// Not found.
	return std::unique_ptr<application::Instance>(nullptr);
}

std::unique_ptr<Server> Request::getServer() {
	return std::move(m_requesterServer);
}

///////////////////////////////////////////////////////////////////////////
// Responder

Responder::Responder(int responderPort, const std::string& name) :
	m_impl(new ResponderImpl(responderPort, name)) {

	// Create the waiting here.
	m_waiting.reset(m_impl->waiting());
}

Responder::~Responder() {
}

std::unique_ptr<Responder> Responder::create(const std::string& name) {

	std::string portName = ResponderImpl::RESPONDER_PREFIX + name;
	json::Object response = application::This::getCom().requestJSON(createRequestPortV0Request(application::This::m_instance.m_id, portName));

	int responderPort = response[message::RequestResponse::VALUE].GetInt();
	if (responderPort == -1) {
		throw ResponderCreationException(response[message::RequestResponse::MESSAGE].GetString());
	}

	return std::unique_ptr<Responder>(new Responder(responderPort, name));
}

const std::string& Responder::getName() const {
	return m_impl->m_name;
}

void Responder::cancel() {
	m_impl->cancel();
}

std::unique_ptr<Request> Responder::receive() {

	std::unique_ptr<RequestImpl> requestImpl = m_impl->receive();
	if (requestImpl.get() == nullptr) {
		return std::unique_ptr<Request>(nullptr);
	}
	return std::unique_ptr<Request>(new Request(requestImpl));
}

bool Responder::isCanceled() const {
	return m_impl->m_canceled;
}

///////////////////////////////////////////////////////////////////////////
// Requester

Requester::Requester(const Endpoint &endpoint, int requesterPort, int responderPort, const std::string& name, int responderId, int requesterId) :
	m_impl(new RequesterImpl(endpoint, requesterPort, responderPort, name, responderId, requesterId)) {

	// Create the waiting here.
	m_waiting.reset(m_impl->waiting());
}

Requester::~Requester() {
}

std::unique_ptr<Requester> Requester::create(application::Instance & instance, const std::string& name) {

	int responderId = instance.getId();
	std::string responderPortName = ResponderImpl::RESPONDER_PREFIX + name;
	int requesterId = RequesterImpl::newRequesterId();
	std::string requesterPortName = RequesterImpl::getRequesterPortName(name, responderId, requesterId);

	std::string request = createConnectPortV0Request(responderId, responderPortName);

	json::Object response = instance.getCom().requestJSON(request);

	int responderPort = response[message::RequestResponse::VALUE].GetInt();
	if (responderPort == -1) {
		// Wait for the responder port.
		instance.waitFor(responderPortName);

		// Retry to connect.
		response = instance.getCom().requestJSON(request);

		responderPort = response[message::RequestResponse::VALUE].GetInt();
		if (responderPort == -1) {
			throw RequesterCreationException(response[message::RequestResponse::MESSAGE].GetString());
		}
	}

	// Request a requester port.
	response = application::This::getCom().requestJSON(createRequestPortV0Request(application::This::m_instance.m_id, requesterPortName));

	int requesterPort = response[message::RequestResponse::VALUE].GetInt();
	if (requesterPort == -1) {
		throw RequesterCreationException(response[message::RequestResponse::MESSAGE].GetString());
	}

	// TODO simplify the use of some variables: responderUrl.
	return std::unique_ptr<Requester>(new Requester(instance.getEndpoint(), requesterPort, responderPort, name, responderId, requesterId));
}

const std::string& Requester::getName() const {
	return m_impl->m_name;
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
	return m_impl->m_canceled;
}

std::ostream& operator<<(std::ostream& os, const Request& request) {

	os << "[endpoint=" << request.m_impl->m_requesterEndpoint
			<< ", id=" << request.m_impl->m_requesterApplicationId << "]";

	return os;
}

std::ostream& operator<<(std::ostream& os, const Responder& responder) {

	os << "rep." << responder.getName()
		<< ":" << application::This::getName()
		<< "." << application::This::getId()
		<< "@" << application::This::getEndpoint();

	return os;
}

std::ostream& operator<<(std::ostream& os, const Requester& requester) {

	os << "req." << requester.getName()
		<< "." << requester.m_impl->m_requesterId
		<< ":" << application::This::getName()
		<< "." << application::This::getId()
		<< "@" << application::This::getEndpoint();

	return os;
}

}
}
