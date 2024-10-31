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

	friend std::ostream& operator<<(std::ostream&, const Response&);

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

/**
 * Stream operator for a Response object.
 */
std::ostream& operator<<(std::ostream&, const Response&);

}

#endif
