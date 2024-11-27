/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

#ifndef CAMEO_BADFORMATEXCEPTION_H_
#define CAMEO_BADFORMATEXCEPTION_H_

#include "RemoteException.h"

namespace cameo {

/**
 * Exception for a bad format.
 */
class CAMEO_EXPORT BadFormatException : public RemoteException {

public:
	/**
	 * Constructor.
	 * \param message The message.
	 */
	BadFormatException(const std::string& message);
};

}

#endif