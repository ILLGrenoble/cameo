/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

#include "EventStreamSocket.h"

#include "ImplFactory.h"
#include "KeyEvent.h"
#include "ResultEvent.h"
#include "StatusEvent.h"
#include "JSON.h"
#include "Messages.h"
#include "impl/zmq/EventStreamSocketZmq.h"

namespace cameo {

EventStreamSocket::EventStreamSocket() {
	m_impl = ImplFactory::createEventStreamSocket();
}

EventStreamSocket::~EventStreamSocket() {
}

void EventStreamSocket::terminate() {
	m_impl.reset();
}

void EventStreamSocket::init(Context * context, const Endpoint& endpoint, RequestSocket * requestSocket) {
	m_impl->init(context, endpoint, requestSocket);
}

std::unique_ptr<Event> EventStreamSocket::receive(bool blocking) {

	std::string message {m_impl->receive(blocking)};

	// In case of non-blocking call, the message can be null.
	if (message.empty()) {
		return {};
	}

	if (message == message::Event::STATUS) {

		message = m_impl->receive();

		// Get the JSON event.
		json::Object event;
		json::parse(event, message);

		int id {event[message::StatusEvent::ID].GetInt()};
		std::string name {event[message::StatusEvent::NAME].GetString()};
		state::Value state {event[message::StatusEvent::APPLICATION_STATE].GetInt()};
		state::Value pastStates {event[message::StatusEvent::PAST_APPLICATION_STATES].GetInt()};

		if (event.HasMember(message::StatusEvent::EXIT_CODE)) {
			return std::make_unique<StatusEvent>(id, name, state, pastStates, event[message::StatusEvent::EXIT_CODE].GetInt());
		}
		return std::make_unique<StatusEvent>(id, name, state, pastStates);
	}
	else if (message == message::Event::RESULT) {

		message = m_impl->receive();

		// Get the JSON event.
		json::Object event;
		json::parse(event, message);

		int id {event[message::ResultEvent::ID].GetInt()};
		std::string name {event[message::ResultEvent::NAME].GetString()};

		// Get the data in the next part.
		message = m_impl->receive();

		return std::make_unique<ResultEvent>(id, name, message);
	}
	else if (message == message::Event::KEYVALUE) {

		message = m_impl->receive();

		// Get the JSON event.
		json::Object event;
		json::parse(event, message);

		int id {event[message::KeyEvent::ID].GetInt()};
		std::string name {event[message::KeyEvent::NAME].GetString()};
		long status {event[message::KeyEvent::STATUS].GetInt()};
		std::string key {event[message::KeyEvent::KEY].GetString()};
		std::string value {event[message::KeyEvent::VALUE].GetString()};

		if (status == message::STORE_KEY_VALUE) {
			return std::make_unique<KeyEvent>(id, name, KeyEvent::Status::STORED, key, value);
		}
		else {
			return std::make_unique<KeyEvent>(id, name, KeyEvent::Status::REMOVED, key, value);
		}
	}
	else if (message == message::Event::CANCEL) {

		message = m_impl->receive();

		// Exit with a null event.
		return {};
	}

	return {};
}

void EventStreamSocket::cancel() {
	m_impl->cancel();
}

}