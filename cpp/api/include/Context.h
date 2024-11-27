/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

#ifndef CAMEO_CONTEXT_H_
#define CAMEO_CONTEXT_H_

#include "Defines.h"

namespace cameo {

/**
 * Base class for the communication context.
 */
class CAMEO_EXPORT Context {

public:
	/**
	 * Constructor.
	 */
	Context() {};

	/**
	 * Destructor.
	 */
	virtual ~Context() {};
};

}

#endif