/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

#include "InvalidArgumentException.h"

namespace cameo {

InvalidArgumentException::InvalidArgumentException(const std::string& message) :
	RemoteException(message) {
}

}