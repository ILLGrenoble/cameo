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

#ifndef CAMEO_STATUSEVENT_H_
#define CAMEO_STATUSEVENT_H_

#include <iostream>
#include "Application.h"
#include "Event.h"

namespace cameo {

class StatusEvent : public Event {

	friend std::ostream& operator<<(std::ostream&, const StatusEvent&);

public:
	StatusEvent(int id, const std::string& name, application::State state, application::State pastStates);
	StatusEvent(const StatusEvent& event);

	virtual StatusEvent* clone();

	application::State getState() const;
	application::State getPastStates() const;

private:
	application::State m_state;
	application::State m_pastStates;
};

std::ostream& operator<<(std::ostream&, const StatusEvent&);

}

#endif
