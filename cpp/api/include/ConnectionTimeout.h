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

#ifndef CAMEO_CONNECTIONTIMEOUT_H_
#define CAMEO_CONNECTIONTIMEOUT_H_

#include <stdexcept>

namespace cameo {

/**
 * Exception for a connection timeout.
 */
class ConnectionTimeout : public std::runtime_error {

public:
	/**
	 * Constructor.
	 */
	ConnectionTimeout(const std::string& endpoint);
};

}

#endif
