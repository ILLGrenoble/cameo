/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

#include "EventThread.h"

#include "Server.h"
#include "EventStreamSocket.h"
#include "EventListener.h"

namespace cameo {

EventThread::EventThread(Server * server, std::unique_ptr<EventStreamSocket>& socket) :
	m_server{server} {
	m_socket = std::move(socket);
}

EventThread::~EventThread() {

	if (m_thread) {
		m_thread->join();
	}
}

void EventThread::start() {

	m_thread.reset(new std::thread{[this] {

		while (true) {
			std::unique_ptr<Event> event {m_socket->receive()};

			if (!event) {
				// The stream is canceled.
				return;
			}

			// Forward the event to the listeners.
			auto eventListeners {m_server->getEventListeners()};
			for (auto listener : eventListeners) {

				// If the application name is null, all the status are pushed, otherwise, filter on the name.
				// We filter on the name because the id is not known at the registration.
				if (!listener.isFiltered()
					|| listener.getListener()->getName() == event->getName()) {

					// Clone the event is necessary because the event is passed to different listeners working in different threads.
					std::unique_ptr<Event> clonedEvent {event->clone()};

					// Push the cloned event.
					listener.getListener()->pushEvent(clonedEvent);
				}
			}
		}
	}});
}

void EventThread::cancel() {
	m_socket->cancel();
}

}