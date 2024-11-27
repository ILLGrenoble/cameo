/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

#include "EventListener.h"

#include "CancelEvent.h"

namespace cameo {

EventListener::EventListener() {
}

EventListener::~EventListener() {
}

void EventListener::setName(const std::string& name) {
	m_name = name;
}

const std::string& EventListener::getName() const {
	return m_name;
}

void EventListener::pushEvent(std::unique_ptr<Event>& event) {
	m_eventQueue.push(event);
}

std::unique_ptr<Event> EventListener::popEvent(bool blocking, int timeout) {

	if (blocking) {
		return m_eventQueue.pop(timeout);
	}
	return m_eventQueue.poll();
}

void EventListener::cancel(int id) {
	std::unique_ptr<Event> event {new CancelEvent(id, m_name)};
	m_eventQueue.push(event);
}

}