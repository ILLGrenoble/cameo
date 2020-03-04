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

#include "StreamSocketImpl.h"

#include <iostream>

using namespace std;

namespace cameo {

const std::string StreamSocketImpl::CANCEL = "CANCEL";

StreamSocketImpl::StreamSocketImpl(zmq::socket_t * socket, zmq::socket_t * cancelSocket) :
	m_socket(socket), m_cancelSocket(cancelSocket) {
}

StreamSocketImpl::~StreamSocketImpl() {
	close();
}

void StreamSocketImpl::send(const std::string& data) {

	zmq::message_t messageData(data.size());
	memcpy((void *) messageData.data(), data.c_str(), data.size());
	m_socket->send(messageData);
}

std::unique_ptr<zmq::message_t> StreamSocketImpl::receive(bool blocking) {

	// Use the message interface.
	unique_ptr<zmq::message_t> message(new zmq::message_t());
	if (m_socket->recv(message.get(), (blocking ? 0 : ZMQ_DONTWAIT))) {
		// The message exists.
		return message;
	}

	return unique_ptr<zmq::message_t>(nullptr);
}

void StreamSocketImpl::cancel() {
	if (m_cancelSocket.get() != nullptr) {
		zmq::message_t requestType(CANCEL.length());
		string data("cancel");
		zmq::message_t requestData(data.length());
		memcpy((void *) requestType.data(), CANCEL.c_str(), CANCEL.length());
		memcpy((void *) requestData.data(), data.c_str(), data.length());
		m_cancelSocket->send(requestType, ZMQ_SNDMORE);
		m_cancelSocket->send(requestData);
	}
}

void StreamSocketImpl::close() {
	m_socket->close();
}

}
