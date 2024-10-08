/*
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under the EUPL, Version 1.1 only (the "License");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */

#ifndef CAMEO_REMOTEEXCEPTION_H_
#define CAMEO_REMOTEEXCEPTION_H_

#include <stdexcept>
#include <string>

namespace cameo {

/**
 * Base class for remote exception.
 */
class RemoteException : public std::exception {

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
