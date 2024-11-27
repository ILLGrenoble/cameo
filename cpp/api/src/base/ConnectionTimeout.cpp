/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

#include "ConnectionTimeout.h"

namespace cameo {

ConnectionTimeout::ConnectionTimeout(const std::string& endpoint) :
	std::runtime_error{"Timeout while connecting " + endpoint} {
}

}