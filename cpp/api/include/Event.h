/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

#ifndef CAMEO_EVENT_H_
#define CAMEO_EVENT_H_

#include "Defines.h"
#include <string>

namespace cameo {

/**
 * Base class for events.
 */
class CAMEO_EXPORT Event {

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