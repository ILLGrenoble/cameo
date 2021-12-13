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

#include "JSON.h"
#include "Messages.h"
#include "impl/zmq/OutputStreamSocketZmq.h"
#include <iostream>

namespace cameo {

Output::Output() :
	m_id(0), m_endOfLine(false) {
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

OutputStreamSocket::OutputStreamSocket(Server * server, const std::string& name) :
	m_applicationId(-1),
	m_ended(false),
	m_canceled(false) {

	//TODO Replace with factory.
	m_impl = std::unique_ptr<StreamSocketImpl>(new OutputStreamSocketZmq(server, name));
	m_impl->init();
}

OutputStreamSocket::~OutputStreamSocket() {
}

void OutputStreamSocket::setApplicationId(int id) {
	m_applicationId = id;
}

std::optional<Output> OutputStreamSocket::receive() {

	// Loop on receive() because in case of configuration multiple=yes, messages can come from different instances.
	while (true) {
		std::string messageType(m_impl->receive());

		// Cancel can only come from this instance.
		if (messageType == message::Event::CANCEL) {
			m_canceled = true;
			return {};
		}

		// Get the second part of the message.
		std::string message = m_impl->receive();

		// Continue if type of message is SYNCSTREAM. Theses messages are only used for the poller.
		if (messageType == message::Event::SYNCSTREAM) {
			continue;
		}

		// Get the JSON event.
		json::Object event;
		json::parse(event, message);

		int id = event[message::ApplicationStream::ID].GetInt();

		// Filter on the application id so that only the messages concerning the instance applicationId are processed.
		// Others are ignored.
		if (m_applicationId == -1 || m_applicationId == id) {

			// Terminate the stream if type of message is ENDSTREAM.
			if (messageType == message::Event::ENDSTREAM) {
				m_ended = true;
				return {};
			}

			// Here the type of message is STREAM.
			std::string line = event[message::ApplicationStream::MESSAGE].GetString();
			bool endOfLine = event[message::ApplicationStream::EOL].GetBool();

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

bool OutputStreamSocket::isEnded() const {
	return m_ended;
}

bool OutputStreamSocket::isCanceled() const {
	return m_canceled;
}

}
