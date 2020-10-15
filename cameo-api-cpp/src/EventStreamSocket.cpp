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

#include "JSON.h"
#include "impl/SocketWaitingImpl.h"
#include "PortEvent.h"
#include "PublisherEvent.h"
#include "ResultEvent.h"
#include "StatusEvent.h"
#include "StoreKeyValueEvent.h"
#include "impl/StreamSocketImpl.h"
#include "message/Message.h"
#include "RemoveKeyValueEvent.h"

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

	if (response == message::Event::STATUS) {

		message = m_impl->receive();

		// Get the JSON event.
		json::Object event;
		json::parse(event, message.get());

		int id = event[message::StatusEvent::ID].GetInt();
		string name = event[message::StatusEvent::NAME].GetString();
		application::State state = event[message::StatusEvent::APPLICATION_STATE].GetInt();
		application::State pastStates = event[message::StatusEvent::PAST_APPLICATION_STATES].GetInt();

		if (event.HasMember(message::StatusEvent::EXIT_CODE)) {
			return unique_ptr<Event>(new StatusEvent(id, name, state, pastStates, event[message::StatusEvent::EXIT_CODE].GetInt()));
		}
		return unique_ptr<Event>(new StatusEvent(id, name, state, pastStates));
	}
	else if (response == message::Event::RESULT) {

		message = m_impl->receive();

		// Get the JSON event.
		json::Object event;
		json::parse(event, message.get());

		int id = event[message::ResultEvent::ID].GetInt();
		string name = event[message::ResultEvent::NAME].GetString();

		// Get the data in the next part.
		message = m_impl->receive();
		string data(message->data<char>(), message->size());

		return unique_ptr<Event>(new ResultEvent(id, name, data));
	}
	else if (response == message::Event::PUBLISHER) {

		message = m_impl->receive();

		// Get the JSON event.
		json::Object event;
		json::parse(event, message.get());

		int id = event[message::PublisherEvent::ID].GetInt();
		string name = event[message::PublisherEvent::NAME].GetString();
		string publisherName = event[message::PublisherEvent::PUBLISHER_NAME].GetString();

		return unique_ptr<Event>(new PublisherEvent(id, name, publisherName));
	}
	else if (response == message::Event::PORT) {

		message = m_impl->receive();

		// Get the JSON event.
		json::Object event;
		json::parse(event, message.get());

		int id = event[message::PortEvent::ID].GetInt();
		string name = event[message::PortEvent::NAME].GetString();
		string portName = event[message::PortEvent::PORT_NAME].GetString();

		return unique_ptr<Event>(new PortEvent(id, name, portName));
	}
	else if (response == message::Event::STOREKEYVALUE) {

		message = m_impl->receive();

		// Get the JSON event.
		json::Object event;
		json::parse(event, message.get());

		int id = event[message::StoreKeyValueEvent::ID].GetInt();
		string name = event[message::StoreKeyValueEvent::NAME].GetString();
		string key = event[message::StoreKeyValueEvent::KEY].GetString();
		string value = event[message::StoreKeyValueEvent::VALUE].GetString();

		return unique_ptr<Event>(new StoreKeyValueEvent(id, name, key, value));
	}
	else if (response == message::Event::REMOVEKEYVALUE) {

		message = m_impl->receive();

		// Get the JSON event.
		json::Object event;
		json::parse(event, message.get());

		int id = event[message::RemoveKeyValueEvent::ID].GetInt();
		string name = event[message::RemoveKeyValueEvent::NAME].GetString();
		string key = event[message::RemoveKeyValueEvent::KEY].GetString();
		string value = event[message::RemoveKeyValueEvent::VALUE].GetString();

		return unique_ptr<Event>(new RemoveKeyValueEvent(id, name, key, value));
	}
	else if (response == message::Event::CANCEL) {

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
	return new SocketWaitingImpl(m_impl->m_cancelSocket.get(), message::Event::CANCEL);
}

}
