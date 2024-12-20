/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

#include "OutputStreamSocket.h"

#include "ImplFactory.h"
#include "JSON.h"
#include "Messages.h"
#include "impl/zmq/OutputStreamSocketZmq.h"
#include <iostream>


namespace cameo {

Output::Output() :
	m_id{0},
	m_endOfLine{false} {
}

int Output::getId() const {
	return m_id;
}

const std::string& Output::getMessage() const {
	return m_message;
}

bool Output::isEndOfLine() const {
	return m_endOfLine;
}

std::string Output::toString() const {
	json::StringObject jsonObject;

	jsonObject.pushKey("id");
	jsonObject.pushValue(m_id);

	jsonObject.pushKey("message");
	jsonObject.pushValue(m_message);

	jsonObject.pushKey("eol");
	jsonObject.pushValue(m_endOfLine);

	return jsonObject.dump();
}

OutputStreamSocket::OutputStreamSocket(const std::string& name) :
	m_applicationId{-1},
	m_ended{false},
	m_canceled{false} {

	m_impl = ImplFactory::createOutputStreamSocket(name);
}

void OutputStreamSocket::init(Context * context, const Endpoint& endpoint, RequestSocket * requestSocket) {
	m_impl->init(context, endpoint, requestSocket);
}

OutputStreamSocket::~OutputStreamSocket() {
}

void OutputStreamSocket::terminate() {
	m_impl.reset();
}

void OutputStreamSocket::setApplicationId(int id) {
	m_applicationId = id;
}

std::optional<Output> OutputStreamSocket::receive() {

	// Loop on receive() because in case of configuration multiple=yes, messages can come from different instances.
	while (true) {
		std::string messageType {m_impl->receive()};

		// Cancel can only come from this instance.
		if (messageType == message::Event::CANCEL) {
			m_canceled = true;
			return {};
		}

		// Get the second part of the message.
		std::string message {m_impl->receive()};

		// Get the JSON event.
		json::Object event;
		json::parse(event, message);

		int type {event[message::TYPE].GetInt()};

		// Continue if type of message is SYNC_STREAM. Theses messages are only used for the poller.
		if (type == message::SYNC_STREAM) {
			continue;
		}

		int id {event[message::ApplicationStream::ID].GetInt()};

		// Filter on the application id so that only the messages concerning the instance applicationId are processed.
		// Others are ignored.
		if (m_applicationId == -1 || m_applicationId == id) {

			// Terminate the stream if type of message is ENDSTREAM.
			if (type == message::STREAM_END) {
				m_ended = true;
				return {};
			}

			// Here the type of message is STREAM.
			std::string line {event[message::ApplicationStream::MESSAGE].GetString()};
			bool endOfLine {event[message::ApplicationStream::EOL].GetBool()};

			Output output;
			output.m_id = id;
			output.m_message = line;
			output.m_endOfLine = endOfLine;

			return std::optional<Output>(output);
		}

		// Here, the application id is different from id, then re-iterate.
	}

	return {};
}

void OutputStreamSocket::cancel() {
	m_impl->cancel();
}

bool OutputStreamSocket::hasEnded() const {
	return m_ended;
}

bool OutputStreamSocket::isCanceled() const {
	return m_canceled;
}

}