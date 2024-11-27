/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

#ifndef CAMEO_REMOTEEXCEPTION_H_
#define CAMEO_REMOTEEXCEPTION_H_

#include "Defines.h"
#include <stdexcept>
#include <string>

namespace cameo {

/**
 * Base class for remote exception.
 */
class CAMEO_EXPORT RemoteException : public std::exception {

public:
	/**
	 * Constructor.
	 * \param message The message.
	 */
	RemoteException(const std::string& message);

	/**
	 * Destructor.
	 */
	virtual ~RemoteException() throw() = 0;

	/**
	 * Function returning the message.
	 * \return The message.
	 */
	virtual const char* what() const throw();

private:
	std::string m_message;
};

}

#endif