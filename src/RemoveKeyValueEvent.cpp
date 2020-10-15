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

#include <iostream>
#include "RemoveKeyValueEvent.h"

namespace cameo {

RemoveKeyValueEvent::RemoveKeyValueEvent(int id, const std::string& name, const std::string& key, const std::string& value) :
	KeyEvent(id, name, key, value) {
}

RemoveKeyValueEvent::RemoveKeyValueEvent(const RemoveKeyValueEvent& event) :
	KeyEvent(event) {
}

RemoveKeyValueEvent* RemoveKeyValueEvent::clone() {
	return new RemoveKeyValueEvent(*this);
}

std::ostream& operator<<(std::ostream& os, const cameo::RemoveKeyValueEvent& event) {
	os << "name=" << event.m_name
		<< "\nid=" << event.m_id
		<< "\nkey=" << event.m_key
		<< "\nvalue=" << event.m_value;

	return os;
}

}
