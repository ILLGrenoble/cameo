/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

#ifndef CAMEO_KEYEVENT_H_
#define CAMEO_KEYEVENT_H_

#include "Event.h"

#include <iostream>

namespace cameo {

/**
 * Class defining a key event.
 */
class KeyEvent : public Event {

	friend std::ostream& operator<<(std::ostream&, const KeyEvent&);

public:
	/**
	 * Type of the status.
	 */
	enum Status {STORED, REMOVED};

	/**
	 * Constructor.
	 * \param id The application id.
	 * \param name The application name.
	 * \param status The status of the key.
	 * \param key The key.
	 * \param value The value.
	 */
	KeyEvent(int id, const std::string& name, Status status, const std::string& key, const std::string& value);

	/**
	 * Copy constructor.
	 * \param event The event to copy.
	 */
	KeyEvent(const KeyEvent& event);

	/**
	 * Clones the event.
	 * \return The cloned event.
	 */
	virtual KeyEvent* clone();

	/**
	 * Gets the status.
	 * \return The status.
	 */
	Status getStatus() const;

	/**
	 * Gets the key.
	 * \return The key.
	 */
	const std::string& getKey() const;

	/**
	 * Gets the value.
	 * \return The value.
	 */
	const std::string& getValue() const;

	/**
	 * Returns a stringified representation of this event.
	 * \return A stringified representation of this event.
	 */
	std::string toString() const;

private:
	Status m_status;
	std::string m_key;
	std::string m_value;
};

/**
 * Stream operator for a KeyEvent object.
 */
std::ostream& operator<<(std::ostream&, const KeyEvent&);

}

#endif