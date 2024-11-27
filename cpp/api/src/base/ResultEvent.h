/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

#ifndef CAMEO_RESULTEVENT_H_
#define CAMEO_RESULTEVENT_H_

#include "Event.h"

#include <iostream>

namespace cameo {

/**
 * Class defining a result event.
 */
class ResultEvent : public Event {

	friend std::ostream& operator<<(std::ostream&, const ResultEvent&);

public:
	/**
	 * Constructor.
	 * \param id The application id.
	 * \param name The application name.
	 * \param data The result data.
	 */
	ResultEvent(int id, const std::string& name, const std::string& data);

	/**
	 * Copy constructor.
	 * \param event The event to copy.
	 */
	ResultEvent(const ResultEvent& event);

	/**
	 * Clones the event.
	 * \return The cloned event.
	 */
	virtual ResultEvent* clone();

	/**
	 * Gets the result data.
	 * \return The data.
	 */
	const std::string& getData() const;

	/**
	 * Returns a stringified representation of this event.
	 * \return A stringified representation of this event.
	 */
	std::string toString() const;

private:
	std::string m_data;
};

/**
 * Stream operator for a ResultEvent object.
 */
std::ostream& operator<<(std::ostream&, const ResultEvent&);

}

#endif