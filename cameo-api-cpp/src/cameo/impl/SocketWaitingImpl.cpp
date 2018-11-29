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

#include "SocketWaitingImpl.h"

#include <iostream>
#include "../Application.h"
#include "WaitingImplSet.h"

using namespace std;

namespace cameo {

SocketWaitingImpl::SocketWaitingImpl(std::auto_ptr<zmq::socket_t>& socket, const std::string& message) :
	m_socket(socket), m_message(message) {

	// Add the object in the waiting set if This exists.
	if (application::This::m_instance.m_impl != 0) {
		application::This::m_instance.m_waitingSet->add(this);
	}
}

SocketWaitingImpl::~SocketWaitingImpl() {

	// Remove the object in the waiting set if This exists.
	if (application::This::m_instance.m_impl != 0) {
		application::This::m_instance.m_waitingSet->remove(this);
	}

	m_socket->close();
}

void SocketWaitingImpl::cancel() {

	zmq::message_t requestType(m_message.length());
	string data("CANCEL");
	zmq::message_t requestData(data.length());
	memcpy((void *) requestType.data(), m_message.c_str(), m_message.length());
	memcpy((void *) requestData.data(), data.c_str(), data.length());
	m_socket->send(requestType, ZMQ_SNDMORE);
	m_socket->send(requestData);
}

}
