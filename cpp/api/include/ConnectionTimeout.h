/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

#ifndef CAMEO_CONNECTIONTIMEOUT_H_
#define CAMEO_CONNECTIONTIMEOUT_H_

#include "Defines.h"
#include <stdexcept>

namespace cameo {

/**
 * Exception for a connection timeout.
 */
class CAMEO_EXPORT ConnectionTimeout : public std::runtime_error {

public:
	/**
	 * Constructor.
	 */
	ConnectionTimeout(const std::string& endpoint);
};

}

#endif