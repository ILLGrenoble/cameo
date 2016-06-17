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

#include "ConnectionHandlerSet.h"
#include "../Server.h"

using namespace std;

namespace cameo {

ConnectionHandlerSet::ConnectionHandlerSet(Server * server) : m_server(server) {
}

ConnectionHandlerSet::~ConnectionHandlerSet() {
	stopThread();
}

void ConnectionHandlerSet::add(std::string const & name, ConnectionHandlerSet::FunctionType handler) {

	boost::mutex::scoped_lock lock(m_mutex);
	m_set[name] = handler;
}

bool ConnectionHandlerSet::remove(std::string const & name) {

	boost::mutex::scoped_lock lock(m_mutex);

	map<string, FunctionType>::iterator h = m_set.find(name);

	if (h != m_set.end()) {
		m_set.erase(h);

		return true;
	}

	return false;
}

void ConnectionHandlerSet::apply(bool available) {

	boost::mutex::scoped_lock lock(m_mutex);

	for (map<string, FunctionType>::const_iterator h = m_set.begin(); h != m_set.end(); ++h) {
		h->second(available);
	}
}

void ConnectionHandlerSet::loop(int timeoutMs, int pollingTimeMs) {

	// Loop until the condition is notified.
	while (true) {
		bool stopped = m_waitCondition.wait(pollingTimeMs);
		if (stopped) {
			return;
		}

		// Check the server.
		bool available = (m_server->isAvailable(timeoutMs));

		// Apply the handlers.
		apply(available);
	}
}

void ConnectionHandlerSet::startThread(int timeoutMs, int pollingTimeMs) {

	// Stop the thread if it exists.
	stopThread();

	// Start the thread.
	m_thread = auto_ptr<boost::thread>(new boost::thread(boost::bind(&ConnectionHandlerSet::loop, this, timeoutMs, pollingTimeMs)));
}

void ConnectionHandlerSet::stopThread() {

	if (m_thread.get() != 0) {
		m_waitCondition.notify();
		m_thread->join();
	}
}

}
