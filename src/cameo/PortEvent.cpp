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

#include "PortEvent.h"

#include <iostream>

namespace cameo {

PortEvent::PortEvent(int id, const std::string& name, const std::string& portName) :
	Event(id, name),
	m_portName(portName) {
}

const std::string& PortEvent::getPortName() const {
	return m_portName;
}

std::ostream& operator<<(std::ostream& os, const cameo::PortEvent& port) {
	os << "name=" << port.m_name
		<< "\nid=" << port.m_id
		<< "\nportName=" << port.m_portName;

	return os;
}

}
