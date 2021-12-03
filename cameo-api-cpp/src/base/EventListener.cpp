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

std::unique_ptr<Event> EventListener::popEvent(bool blocking) {

	if (blocking) {
		return m_eventQueue.pop();
	}
	return m_eventQueue.poll();
}

std::unique_ptr<Event> EventListener::popEvent() {
	return popEvent(true);
}

void EventListener::cancel(int id) {
	std::unique_ptr<Event> event(new CancelEvent(id, m_name));
	m_eventQueue.push(event);
}

}

