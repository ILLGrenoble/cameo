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

#include "StoreKeyValueEvent.h"

#include <iostream>

namespace cameo {

StoreKeyValueEvent::StoreKeyValueEvent(int id, const std::string& name, const std::string& key, const std::string& value) :
	KeyEvent(id, name, key, value) {
}

StoreKeyValueEvent::StoreKeyValueEvent(const StoreKeyValueEvent& event) :
	KeyEvent(event) {
}

StoreKeyValueEvent* StoreKeyValueEvent::clone() {
	return new StoreKeyValueEvent(*this);
}

std::ostream& operator<<(std::ostream& os, const cameo::StoreKeyValueEvent& event) {
	os << "name=" << event.m_name
		<< "\nid=" << event.m_id
		<< "\nkey=" << event.m_key
		<< "\nvalue=" << event.m_value;

	return os;
}

}
