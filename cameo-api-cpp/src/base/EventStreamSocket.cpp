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

#include "KeyEvent.h"
#include "PortEvent.h"
#include "PublisherEvent.h"
#include "ResultEvent.h"
#include "StatusEvent.h"
#include "impl/SocketWaitingImpl.h"
#include "impl/StreamSocketImpl.h"
#include "JSON.h"
#include "Messages.h"

namespace cameo {

EventStreamSocket::EventStreamSocket(StreamSocketImpl * impl) : m_impl(impl) {
}

EventStreamSocket::~EventStreamSocket() {
}

std::unique_ptr<Event> EventStreamSocket::receive(bool blocking) {

	std::unique_ptr<zmq::message_t> message(m_impl->receive(blocking));

	// In case of non-blocking call, the message can be null.
	if (message == nullptr) {
		return std::unique_ptr<Event>(nullptr);
	}

	std::string response(static_cast<char*>(message->data()), message->size());

	if (response == message::Event::STATUS) {

		message = m_impl->receive();

		// Get the JSON event.
		json::Object event;
		json::parse(event, message.get());

		int id = event[message::StatusEvent::ID].GetInt();
		std::string name = event[message::StatusEvent::NAME].GetString();
		application::State state = event[message::StatusEvent::APPLICATION_STATE].GetInt();
		application::State pastStates = event[message::StatusEvent::PAST_APPLICATION_STATES].GetInt();

		if (event.HasMember(message::StatusEvent::EXIT_CODE)) {
			return std::make_unique<StatusEvent>(id, name, state, pastStates, event[message::StatusEvent::EXIT_CODE].GetInt());
		}
		return std::make_unique<StatusEvent>(id, name, state, pastStates);
	}
	else if (response == message::Event::RESULT) {

		message = m_impl->receive();

		// Get the JSON event.
		json::Object event;
		json::parse(event, message.get());

		int id = event[message::ResultEvent::ID].GetInt();
		std::string name = event[message::ResultEvent::NAME].GetString();

		// Get the data in the next part.
		message = m_impl->receive();
		std::string data(message->data<char>(), message->size());

		return std::make_unique<ResultEvent>(id, name, data);
	}
	else if (response == message::Event::PUBLISHER) {

		message = m_impl->receive();

		// Get the JSON event.
		json::Object event;
		json::parse(event, message.get());

		int id = event[message::PublisherEvent::ID].GetInt();
		std::string name = event[message::PublisherEvent::NAME].GetString();
		std::string publisherName = event[message::PublisherEvent::PUBLISHER_NAME].GetString();

		return std::make_unique<PublisherEvent>(id, name, publisherName);
	}
	else if (response == message::Event::PORT) {

		message = m_impl->receive();

		// Get the JSON event.
		json::Object event;
		json::parse(event, message.get());

		int id = event[message::PortEvent::ID].GetInt();
		std::string name = event[message::PortEvent::NAME].GetString();
		std::string portName = event[message::PortEvent::PORT_NAME].GetString();

		return std::make_unique<PortEvent>(id, name, portName);
	}
	else if (response == message::Event::KEYVALUE) {

		message = m_impl->receive();

		// Get the JSON event.
		json::Object event;
		json::parse(event, message.get());

		int id = event[message::KeyEvent::ID].GetInt();
		std::string name = event[message::KeyEvent::NAME].GetString();
		long status = event[message::KeyEvent::STATUS].GetInt64();
		std::string key = event[message::KeyEvent::KEY].GetString();
		std::string value = event[message::KeyEvent::VALUE].GetString();

		if (status == message::STORE_KEY_VALUE) {
			return std::make_unique<KeyEvent>(id, name, KeyEvent::Status::STORED, key, value);
		}
		else {
			return std::make_unique<KeyEvent>(id, name, KeyEvent::Status::REMOVED, key, value);
		}
	}
	else if (response == message::Event::CANCEL) {

		message = m_impl->receive();

		// Exit with a null event.
		return std::unique_ptr<Event>(nullptr);
	}

	std::cerr << "Cannot process '" << response << "' event" << std::endl;
	return std::unique_ptr<Event>(nullptr);
}

void EventStreamSocket::cancel() {
	m_impl->cancel();
}

WaitingImpl * EventStreamSocket::waiting() {
	// We transfer the ownership of cancel socket to WaitingImpl
	return new SocketWaitingImpl(m_impl->m_cancelSocket.get(), message::Event::CANCEL);
}

}