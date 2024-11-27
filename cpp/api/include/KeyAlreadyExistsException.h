/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

#ifndef CAMEO_KEYALREADYEXISTSEXCEPTION_H_
#define CAMEO_KEYALREADYEXISTSEXCEPTION_H_

#include "RemoteException.h"

namespace cameo {

/**
 * Exception for a key that already exists.
 */
class CAMEO_EXPORT KeyAlreadyExistsException : public RemoteException {

public:
	/**
	 * Constructor.
	 * \param message The message.
	 */
	KeyAlreadyExistsException(const std::string& message);
};

}

#endif