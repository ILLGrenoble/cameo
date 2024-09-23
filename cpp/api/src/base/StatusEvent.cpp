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
#include "JSON.h"
#include <iostream>

namespace cameo {

StatusEvent::StatusEvent(int id, const std::string& name, state::Value state, state::Value pastStates, int exitCode) :
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

state::Value StatusEvent::getState() const {
	return m_state;
}

state::Value StatusEvent::getPastStates() const {
	return m_pastStates;
}

int StatusEvent::getExitCode() const {
	return m_exitCode;
}

std::string StatusEvent::toString() const {
	json::StringObject jsonObject;

	jsonObject.pushKey("type");
	jsonObject.pushValue("status");

	jsonObject.pushKey("id");
	jsonObject.pushValue(m_id);

	jsonObject.pushKey("name");
	jsonObject.pushValue(m_name);

	jsonObject.pushKey("state");
	jsonObject.pushValue(cameo::toString(m_state));

	jsonObject.pushKey("past_states");
	jsonObject.pushValue(cameo::toString(m_pastStates));

	return jsonObject.dump();
}

std::ostream& operator<<(std::ostream& os, const cameo::StatusEvent& status) {
	os << status.toString();

	return os;
}

}
