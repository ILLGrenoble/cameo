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
	 * Constructor.
	 */
	Pingable();

	/**
	 * Destructor.
	 */
	virtual ~Pingable();

	/**
	 * Pings with response.
	 * \return true if there is no timeout.
	 */
	virtual bool ping() = 0;
};

}

#endif
