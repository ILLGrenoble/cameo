/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

#ifndef CAMEO_APPEXCEPTION_H_
#define CAMEO_APPEXCEPTION_H_

#include "RemoteException.h"

namespace cameo {

/**
 * Exception when starting an App.
 */
class CAMEO_EXPORT StartException : public RemoteException {

public:
	/**
	 * Constructor.
	 * \param message The message.
	 */
	StartException(const std::string& message);
};

}

#endif