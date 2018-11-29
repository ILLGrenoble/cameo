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

StatusEvent::StatusEvent(int id, const std::string& name, application::State state, application::State pastStates) :
	Event(id, name),
	m_state(state),
	m_pastStates(pastStates) {
}

application::State StatusEvent::getState() const {
	return m_state;
}

application::State StatusEvent::getPastStates() const {
	return m_pastStates;
}

std::ostream& operator<<(std::ostream& os, const cameo::StatusEvent& status) {
	os << "name=" << status.m_name
		<< "\nid=" << status.m_id
		<< "\nstate=" << status.m_state;

	return os;
}

}
