/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

#include "ConnectionChecker.h"

#include "Server.h"
#include "TimeCondition.h"

namespace cameo {

ConnectionChecker::ConnectionChecker(Server * server, ConnectionChecker::FunctionType handler) : m_server(server), m_function(handler) {
	m_waitCondition.reset(new TimeCondition{});
}

ConnectionChecker::~ConnectionChecker() {
	stopThread();
}

void ConnectionChecker::loop(int timeoutMs, int pollingTimeMs) {

	// Loop until the condition is notified.
	while (true) {
		bool stopped {m_waitCondition->wait(pollingTimeMs)};
		if (stopped) {
			return;
		}

		// Check the server.
		bool available {m_server->isAvailable(timeoutMs)};

		// Apply the handler.
		m_function(available);
	}
}

void ConnectionChecker::startThread(int timeoutMs, int pollingTimeMs) {

	// Stop the thread if it exists.
	stopThread();

	// Start the thread.
	m_thread = std::unique_ptr<std::thread>{new std::thread(std::bind(&ConnectionChecker::loop, this, timeoutMs, pollingTimeMs))};
}

void ConnectionChecker::stopThread() {

	if (m_thread) {
		m_waitCondition->notify();
		m_thread->join();
	}
}

}