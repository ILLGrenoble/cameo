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

#include "OutputStreamSocket.h"

#include "impl/SocketImpl.h"
#include "impl/SocketWaitingImpl.h"
#include "impl/ServicesImpl.h"
#include "../proto/Messages.pb.h"
#include <iostream>

using namespace std;

namespace cameo {

Output::Output() :
	m_id(0) {
}

int Output::getId() const {
	return m_id;
}

const std::string& Output::getMessage() const {
	return m_message;
}

OutputStreamSocket::OutputStreamSocket(SocketImpl * impl) :
	m_ended(false),
	m_canceled(false),
	m_impl(impl) {
}

OutputStreamSocket::~OutputStreamSocket() {
}

bool OutputStreamSocket::receive(Output& output) {

	unique_ptr<zmq::message_t> message(m_impl->receive());

	string response(static_cast<char*>(message->data()), message->size());

	if (response == ServicesImpl::STREAM) {
	}
	else if (response == ServicesImpl::ENDSTREAM) {
		m_ended = true;
		return false;
	}
	else if (response == ServicesImpl::CANCEL) {
		m_canceled = true;
		return false;
	}

	message = m_impl->receive();

	proto::ApplicationStream protoStream;
	protoStream.ParseFromArray(message->data(), message->size());

	output.m_id = protoStream.id();
	output.m_message = protoStream.message();

	return true;
}

void OutputStreamSocket::cancel() {
	m_impl->cancel();
}

bool OutputStreamSocket::isEnded() const {
	return m_ended;
}

bool OutputStreamSocket::isCanceled() const {
	return m_canceled;
}

WaitingImpl * OutputStreamSocket::waiting() {
	// We transfer the ownership of cancel socket to WaitingImpl
	return new SocketWaitingImpl(m_impl->m_cancelSocket.get(), "CANCEL");
}

}
