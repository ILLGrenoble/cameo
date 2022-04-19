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

#ifndef CAMEO_APPEXCEPTION_H_
#define CAMEO_APPEXCEPTION_H_

#include "RemoteException.h"

namespace cameo {

/**
 * Exception for an App.
 */
class AppException : public RemoteException {

public:
	/**
	 * Constructor.
	 * \param message The message.
	 */
	AppException(const std::string& message);
};

/**
 * Exception when starting an App.
 */
class AppStartException : public RemoteException {

public:
	/**
	 * Constructor.
	 * \param message The message.
	 */
	AppStartException(const std::string& message);
};

/**
 * Exception when connecting an App.
 */
class AppConnectException : public RemoteException {

public:
	/**
	 * Constructor.
	 * \param message The message.
	 */
	AppConnectException(const std::string& message);
};

}

#endif
