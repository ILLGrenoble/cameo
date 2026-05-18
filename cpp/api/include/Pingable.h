/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

#ifndef CAMEO_PINGABLE_H_
#define CAMEO_PINGABLE_H_

#include "Defines.h"

namespace cameo {

/**
 * Class defining an interface for pingable objects.
 */
class CAMEO_EXPORT Pingable {

public:
	/**
	 * Destructor.
	 */
	virtual ~Pingable() {}

	/**
	 * Registers the pingable.
	 */
	void init();

	/**
	 * Unregisters the pingable.
	 */
	void terminate();

	/**
	 * Pings with response.
	 * \param timeout The timeout.
	 * \return true if there is no timeout.
	 */
	virtual bool ping(int timeout) = 0;
};

}

#endif
