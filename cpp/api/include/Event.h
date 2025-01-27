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

#ifndef CAMEO_EVENT_H_
#define CAMEO_EVENT_H_

#include <string>

namespace cameo {

/**
 * Base class for events.
 */
class Event {

public:
	/**
	 * Constructor.
	 * \param id The application id.
	 * \param name The application name.
	 */
	Event(int id, const std::string& name);

	/**
	 * Copy constructor.
	 * \param event The event to copy.
	 */
	Event(const Event& event);

	/**
	 * Destructor.
	 */
	virtual ~Event();

	/**
	 * Clones the event.
	 * \return The cloned event.
	 */
	virtual Event* clone() = 0;

	/**
	 * Gets the application id.
	 * \return The application id.
	 */
	int getId() const;

	/**
	 * Gets the application name.
	 * \return The application name.
	 */
	const std::string& getName() const;

protected:
	int m_id;
	std::string m_name;
};

}

#endif
