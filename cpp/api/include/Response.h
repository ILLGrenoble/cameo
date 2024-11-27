/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

#ifndef CAMEO_RESPONSE_H_
#define CAMEO_RESPONSE_H_

#include "Defines.h"
#include <string>
#include <iostream>

namespace cameo {

/**
 * Class defining a response.
 */
class CAMEO_EXPORT Response {

public:
	/**
	 * Constructor.
	 */
	Response();

	/**
	 * Constructor.
	 * \param value The value.
	 * \param message The message.
	 */
	Response(int value, const std::string& message);

	/**
	 * Gets the value.
	 * \return The value.
	 */
	int getValue() const;

	/**
	 * Gets the message.
	 * \return The message.
	 */
	const std::string& getMessage() const;

	/**
	 * Convenient function returning success or not.
	 * \return True if the response is success.
	 */
	bool isSuccess() const;

	/**
	 * Returns a stringified representation of this event.
	 * \return A stringified representation of this event.
	 */
	std::string toString() const;

private:
	int m_value;
	std::string m_message;
};

}

/**
 * Stream operator for a Response object.
 */
CAMEO_EXPORT std::ostream& operator<<(std::ostream&, const cameo::Response&);

#endif