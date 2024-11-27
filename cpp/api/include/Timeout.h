/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

#ifndef CAMEO_TIMEOUT_H_
#define CAMEO_TIMEOUT_H_

#include "Defines.h"
#include <stdexcept>

namespace cameo {

/**
 * Exception for a general timeout.
 */
class CAMEO_EXPORT Timeout : public std::runtime_error {

public:
	/**
	 * Constructor.
	 */
	Timeout();
};

}

#endif