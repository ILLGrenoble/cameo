/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
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