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

#ifndef CAMEO_PORTEVENT_H_
#define CAMEO_PORTEVENT_H_

#include <iostream>
#include "Event.h"

namespace cameo {

class PortEvent : public Event {

	friend std::ostream& operator<<(std::ostream&, const PortEvent&);

public:
	PortEvent(int id, const std::string& name, const std::string& portName);
	PortEvent(const PortEvent& event);

	virtual PortEvent* clone();

	const std::string& getPortName() const;

private:
	std::string m_portName;
};

std::ostream& operator<<(std::ostream&, const PortEvent&);

}

#endif