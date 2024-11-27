/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

#ifndef CAMEO_UNDEFINEDAPPLICATIONEXCEPTION_H_
#define CAMEO_UNDEFINEDAPPLICATIONEXCEPTION_H_

#include "RemoteException.h"

namespace cameo {

/**
 * Exception for an undefined application.
 */
class CAMEO_EXPORT UndefinedApplicationException : public RemoteException {

public:
	/**
	 * Constructor.
	 * \param message The message.
	 */
	UndefinedApplicationException(const std::string& message);
};

}

#endif