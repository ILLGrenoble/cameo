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

#include "SocketImpl.h"

#include <iostream>

using namespace std;

namespace cameo {

const std::string SocketImpl::CANCEL = "CANCEL";

SocketImpl::SocketImpl(zmq::socket_t * socket, zmq::socket_t * cancelSocket) :
	m_socket(socket), m_cancelSocket(cancelSocket) {
}

SocketImpl::~SocketImpl() {
	close();
}

void SocketImpl::send(const std::string& data) {

	zmq::message_t messageData(data.size());
	memcpy((void *) messageData.data(), data.c_str(), data.size());
	m_socket->send(messageData);
}

zmq::message_t * SocketImpl::receive() {
	// use the message interface
	zmq::message_t * message = new zmq::message_t();
	m_socket->recv(message);

	return message;
}

void SocketImpl::cancel() {
	if (m_cancelSocket.get() != 0) {
		zmq::message_t requestType(CANCEL.length());
		string data("cancel");
		zmq::message_t requestData(data.length());
		memcpy((void *) requestType.data(), CANCEL.c_str(), CANCEL.length());
		memcpy((void *) requestData.data(), data.c_str(), data.length());
		m_cancelSocket->send(requestType, ZMQ_SNDMORE);
		m_cancelSocket->send(requestData);
	}
}

void SocketImpl::close() {
	m_socket->close();
}

}
