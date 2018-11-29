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

#include "ConnectionChecker.h"
#include "Server.h"
#include "TimeCondition.h"

using namespace std;

namespace cameo {

ConnectionChecker::ConnectionChecker(Server * server, ConnectionChecker::FunctionType handler) : m_server(server), m_function(handler) {
	m_waitCondition.reset(new TimeCondition());
}

ConnectionChecker::~ConnectionChecker() {
	stopThread();
}

void ConnectionChecker::loop(int timeoutMs, int pollingTimeMs) {

	// Loop until the condition is notified.
	while (true) {
		bool stopped = m_waitCondition->wait(pollingTimeMs);
		if (stopped) {
			return;
		}

		// Check the server.
		bool available = (m_server->isAvailable(timeoutMs));

		// Apply the handler.
		m_function(available);
	}
}

void ConnectionChecker::startThread(int timeoutMs, int pollingTimeMs) {

	// Stop the thread if it exists.
	stopThread();

	// Start the thread.
	m_thread = auto_ptr<boost::thread>(new boost::thread(boost::bind(&ConnectionChecker::loop, this, timeoutMs, pollingTimeMs)));
}

void ConnectionChecker::stopThread() {

	if (m_thread.get() != 0) {
		m_waitCondition->notify();
		m_thread->join();
	}
}

}
