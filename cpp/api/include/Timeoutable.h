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

#ifndef CAMEO_TIMEOUTABLE_H_
#define CAMEO_TIMEOUTABLE_H_

#include "Defines.h"

namespace cameo {

/**
 * Class defining an interface for objects that have a timeout.
 */
class CAMEO_EXPORT Timeoutable {

public:
	/**
	 * Destructor.
	 */
	virtual ~Timeoutable() {}

	/**
	 * Sets the timeout.
	 * \param value The timeout.
	 */
	virtual void setTimeout(int value) = 0;

	/**
	 * Gets the timeout.
	 * \return The timeout.
	 */
	virtual int getTimeout() const = 0;
};

}

#endif

