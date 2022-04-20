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

#include "StatusEvent.h"

#include <iostream>

namespace cameo {

StatusEvent::StatusEvent(int id, const std::string& name, State state, State pastStates, int exitCode) :
	Event{id, name},
	m_state{state},
	m_pastStates{pastStates},
	m_exitCode{exitCode} {
}

StatusEvent::StatusEvent(const StatusEvent& event) :
	Event{event},
	m_state{event.m_state},
	m_pastStates{event.m_pastStates},
	m_exitCode{event.m_exitCode} {
}

StatusEvent* StatusEvent::clone() {
	return new StatusEvent{*this};
}

State StatusEvent::getState() const {
	return m_state;
}

State StatusEvent::getPastStates() const {
	return m_pastStates;
}

int StatusEvent::getExitCode() const {
	return m_exitCode;
}

std::ostream& operator<<(std::ostream& os, const cameo::StatusEvent& status) {
	os << "name=" << status.m_name
		<< "\nid=" << status.m_id
		<< "\nstate=" << status.m_state;

	return os;
}

}
