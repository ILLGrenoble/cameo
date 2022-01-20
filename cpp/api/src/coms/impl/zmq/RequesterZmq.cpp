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

#include "RequesterZmq.h"
#include "Application.h"
#include "Serializer.h"
#include "JSON.h"
#include "../../../base/impl/zmq/ContextZmq.h"
#include "../../../base/Messages.h"
#include "../../../base/RequestSocket.h"
#include <zmq.hpp>
#include <sstream>

namespace cameo {
namespace coms {

const std::string RequesterZmq::REQUESTER_PREFIX = "req.";

void RequesterZmq::init(const Endpoint& endpoint, int requesterPort, int responderPort) {
	m_requesterPort = requesterPort;
	m_canceled = false;

	// Create the request socket.
	m_requestSocket = application::This::getCom().createRequestSocket(endpoint.withPort(responderPort).toString());

	// Create a socket REP.
	ContextZmq* contextImpl = dynamic_cast<ContextZmq *>(application::This::getCom().getContext());
	m_repSocket.reset(new zmq::socket_t(contextImpl->getContext(), ZMQ_REP));
	std::stringstream reqEndpoint;
	reqEndpoint << "tcp://*:" << m_requesterPort;

	m_repSocket->bind(reqEndpoint.str().c_str());
}

RequesterZmq::~RequesterZmq() {
	terminate();
}

void RequesterZmq::sendBinary(const std::string& requestData) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushValue(message::REQUEST);

	request.pushKey(message::Request::APPLICATION_NAME);
	request.pushValue(application::This::getName());

	request.pushKey(message::Request::APPLICATION_ID);
	request.pushValue(application::This::getId());

	request.pushKey(message::Request::SERVER_URL);
	request.pushValue(application::This::getEndpoint().getProtocol() + "://" + application::This::getEndpoint().getAddress());

	request.pushKey(message::Request::SERVER_PORT);
	request.pushValue(application::This::getEndpoint().getPort());

	request.pushKey(message::Request::REQUESTER_PORT);
	request.pushValue(m_requesterPort);

	m_requestSocket->request(request.toString(), requestData);
}

void RequesterZmq::send(const std::string& requestData) {

	// encode the data
	std::string result;
	serialize(requestData, result);
	sendBinary(result);
}

void RequesterZmq::sendTwoBinaryParts(const std::string& requestData1, const std::string& requestData2) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushValue(message::REQUEST);

	request.pushKey(message::Request::APPLICATION_NAME);
	request.pushValue(application::This::getName());

	request.pushKey(message::Request::APPLICATION_ID);
	request.pushValue(application::This::getId());

	request.pushKey(message::Request::SERVER_URL);
	request.pushValue(application::This::getEndpoint().getProtocol() + "://" + application::This::getEndpoint().getAddress());

	request.pushKey(message::Request::SERVER_PORT);
	request.pushValue(application::This::getEndpoint().getPort());

	request.pushKey(message::Request::REQUESTER_PORT);
	request.pushValue(m_requesterPort);

	m_requestSocket->request(request.toString(), requestData1, requestData2);
}

std::optional<std::string> RequesterZmq::receiveBinary() {

	if (m_canceled) {
		return {};
	}

	zmq::message_t message;
	if (!m_repSocket->recv(message, zmq::recv_flags::none).has_value()) {
		return {};
	}

	// Get the JSON request.
	json::Object request;
	json::parse(request, message);

	int type = request[message::TYPE].GetInt();

	if (type == message::CANCEL) {
		m_canceled = true;
		return {};
	}

	std::optional<std::string> result;

	if (type == message::RESPONSE) {
		// Get the second part for the message.
		zmq::message_t secondPart;
		if (!m_repSocket->recv(secondPart, zmq::recv_flags::none).has_value()) {
			return {};
		}
		result = std::string(secondPart.data<char>(), secondPart.size());
	}

	// Create the reply.
	std::string data = createRequestResponse(0, "OK");
	size_t size = data.length();
	std::unique_ptr<zmq::message_t> reply(new zmq::message_t(size));
	memcpy(reply->data(), data.c_str(), size);

	m_repSocket->send(*reply, zmq::send_flags::none);

	return result;
}

std::optional<std::string> RequesterZmq::receive() {
	return receiveBinary();
}

void RequesterZmq::cancel() {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushValue(message::CANCEL);

	// Create a request socket only for the request.
	std::unique_ptr<RequestSocket> requestSocket = application::This::getCom().createRequestSocket(application::This::getEndpoint().withPort(m_requesterPort).toString());
	requestSocket->requestJSON(request.toString());
}

bool RequesterZmq::isCanceled() {
	return m_canceled;
}

void RequesterZmq::terminate() {

	if (m_repSocket.get() != nullptr) {
		m_repSocket.reset(nullptr);
	}

	m_requestSocket.reset();
}

}
}

