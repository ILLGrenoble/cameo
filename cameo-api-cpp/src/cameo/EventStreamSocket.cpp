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

#include "EventStreamSocket.h"

#include "impl/SocketWaitingImpl.h"
#include "PortEvent.h"
#include "PublisherEvent.h"
#include "ResultEvent.h"
#include "StatusEvent.h"
#include "../proto/Messages.pb.h"
#include "impl/StreamSocketImpl.h"

using namespace std;

namespace cameo {

EventStreamSocket::EventStreamSocket(StreamSocketImpl * impl) : m_impl(impl) {
}

EventStreamSocket::~EventStreamSocket() {
}

std::unique_ptr<Event> EventStreamSocket::receive(bool blocking) {

	unique_ptr<zmq::message_t> message(m_impl->receive(blocking));

	// In case of non-blocking call, the message can be null.
	if (message == nullptr) {
		return unique_ptr<Event>(nullptr);
	}

	string response(static_cast<char*>(message->data()), message->size());

	if (response == "STATUS") {

		message = m_impl->receive();

		proto::StatusEvent protoStatus;
		protoStatus.ParseFromArray(message->data(), message->size());

		return unique_ptr<Event>(new StatusEvent(protoStatus.id(), protoStatus.name(), protoStatus.applicationstate(), protoStatus.pastapplicationstates()));

	} else if (response == "RESULT") {

		message = m_impl->receive();

		proto::ResultEvent protoResult;
		protoResult.ParseFromArray(message->data(), message->size());

		return unique_ptr<Event>(new ResultEvent(protoResult.id(), protoResult.name(), protoResult.data()));

	} else if (response == "PUBLISHER") {

		message = m_impl->receive();

		proto::PublisherEvent protoPublisher;
		protoPublisher.ParseFromArray(message->data(), message->size());

		return unique_ptr<Event>(new PublisherEvent(protoPublisher.id(), protoPublisher.name(), protoPublisher.publishername()));

	} else if (response == "PORT") {

		message = m_impl->receive();

		proto::PortEvent protoPort;
		protoPort.ParseFromArray(message->data(), message->size());

		return unique_ptr<Event>(new PortEvent(protoPort.id(), protoPort.name(), protoPort.portname()));

	} else if (response == "CANCEL") {

		message = m_impl->receive();

		// Exit with a null event.
		return unique_ptr<Event>(nullptr);
	}

	cerr << "Cannot process '" << response << "' event" << endl;
	return unique_ptr<Event>(nullptr);
}

void EventStreamSocket::cancel() {
	m_impl->cancel();
}

WaitingImpl * EventStreamSocket::waiting() {
	// We transfer the ownership of cancel socket to WaitingImpl
	return new SocketWaitingImpl(m_impl->m_cancelSocket.get(), "CANCEL");
}

}
