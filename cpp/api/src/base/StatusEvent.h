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

#include "Application.h"
#include "Event.h"

#include <iostream>

namespace cameo {

/**
 * Class defining a status event.
 */
class StatusEvent : public Event {

	friend std::ostream& operator<<(std::ostream&, const StatusEvent&);

public:
	/**
	 * Constructor.
	 * \param id The application id.
	 * \param name The application name.
	 * \param state The current state.
	 * \param pastStates The past states.
	 * \param exitCode The exit code.
	 */
	StatusEvent(int id, const std::string& name, state::Value state, state::Value pastStates, int exitCode = -1);

	/**
	 * Copy constructor.
	 * \param event The event to copy.
	 */
	StatusEvent(const StatusEvent& event);

	/**
	 * Clones the event.
	 * \return The cloned event.
	 */
	virtual StatusEvent* clone();

	/**
	 * Gets the current state.
	 * \return The state.
	 */
	state::Value getState() const;

	/**
	 * Gets the past states.
	 * \return The past states.
	 */
	state::Value getPastStates() const;

	/**
	 * Gets the exit code.
	 * \return The exit code.
	 */
	int getExitCode() const;

	/**
	 * Returns a stringified representation of this event.
	 * \return A stringified representation of this event.
	 */
	std::string toString() const;

private:
	state::Value m_state;
	state::Value m_pastStates;
	int m_exitCode; // TODO replace with optional
};

/**
 * Stream operator for a StatusEvent object.
 */
std::ostream& operator<<(std::ostream&, const StatusEvent&);

}

#endif
