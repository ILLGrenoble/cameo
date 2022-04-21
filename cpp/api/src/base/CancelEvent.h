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

#ifndef CAMEO_CANCELEVENT_H_
#define CAMEO_CANCELEVENT_H_

#include "Event.h"

#include <iostream>

namespace cameo {

/**
 * Class defining a cancel event.
 */
class CancelEvent : public Event {

	friend std::ostream& operator<<(std::ostream&, const CancelEvent&);

public:
	/**
	 * Constructor.
	 * \param id The application id.
	 * \param name The application name.
	 */
	CancelEvent(int id, const std::string& name);

	/**
	 * Copy constructor.
	 * \param event The event to copy.
	 */
	CancelEvent(const CancelEvent& event);

	/**
	 * Clones the event.
	 * \return The cloned event.
	 */
	virtual CancelEvent* clone();

	/**
	 * Returns a stringified representation of this event.
	 * \return A stringified representation of this event.
	 */
	std::string toString() const;
};

/**
 * Stream operator for a CancelEvent object.
 */
std::ostream& operator<<(std::ostream&, const CancelEvent&);

}

#endif
