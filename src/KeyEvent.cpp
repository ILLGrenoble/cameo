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

#include "KeyEvent.h"

#include <iostream>

namespace cameo {

KeyEvent::KeyEvent(int id, const std::string& name, Status status, const std::string& key, const std::string& value) :
	Event(id, name),
	m_status(status),
	m_key(key),
	m_value(value) {
}

KeyEvent::KeyEvent(const KeyEvent& event) :
	Event(event),
	m_status(event.m_status),
	m_key(event.m_key),
	m_value(event.m_value) {
}

KeyEvent* KeyEvent::clone() {
	return new KeyEvent(*this);
}

KeyEvent::Status KeyEvent::getStatus() const {
	return m_status;
}

const std::string& KeyEvent::getKey() const {
	return m_key;
}

const std::string& KeyEvent::getValue() const {
	return m_value;
}

std::ostream& operator<<(std::ostream& os, const KeyEvent& event) {
	os << "name=" << event.m_name
		<< "\nid=" << event.m_id
		<< "\nstatus" << static_cast<int>(event.m_status)
		<< "\nkey=" << event.m_key
		<< "\nvalue=" << event.m_value;

	return os;
}

}
