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
#include "../proto/Messages.pb.h"

using namespace std;

namespace cameo {

Output::Output(int id, const std::string& message, bool end) {
	m_id = id;
	m_message = message;
	m_end = end;
}

int Output::getId() const {
	return m_id;
}

const std::string& Output::getMessage() const {
	return m_message;
}

bool Output::isEnd() const {
	return m_end;
}

OutputStreamSocket::OutputStreamSocket(const std::string& streamString, const std::string& endOfStreamString, SocketImpl * impl) :
	m_streamString(streamString),
	m_endOfStreamString(endOfStreamString),
	m_impl(impl) {
}

OutputStreamSocket::~OutputStreamSocket() {
}


std::unique_ptr<Output> OutputStreamSocket::receive() {

	unique_ptr<zmq::message_t> message(m_impl->receive());

	// In case of non-blocking call, the message can be null.
	if (message == nullptr) {
		return unique_ptr<Output>(nullptr);
	}

	string response(static_cast<char*>(message->data()), message->size());

	bool end = false;

	if (response == m_streamString) {
		end = false;
	}
	else if (response == m_endOfStreamString) {
		end = true;
	}

	message = m_impl->receive();

	proto::ApplicationStream protoStream;
	protoStream.ParseFromArray(message->data(), message->size());

	return unique_ptr<Output>(new Output(protoStream.id(), protoStream.message(), end));
}

void OutputStreamSocket::cancel() {
	m_impl->cancel();
}

WaitingImpl * OutputStreamSocket::waiting() {
	// We transfer the ownership of cancel socket to WaitingImpl
	return new SocketWaitingImpl(m_impl->m_cancelSocket.get(), "CANCEL");
}

}
