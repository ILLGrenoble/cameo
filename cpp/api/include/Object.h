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

#ifndef CAMEO_OBJECT_H_
#define CAMEO_OBJECT_H_

#include <string>

namespace cameo {

/**
 * Class defining an abstract Cameo object that can be initialized and terminated.
 */
class Object {

public:
	/**
	 * Destructor.
	 */
	virtual ~Object() {}

	/**
	 * Initializes the object.
	 */
	virtual void init() = 0;

	/**
	 * Terminates the object.
	 */
	virtual void terminate() = 0;

	/**
	 * Returns a string representation of this application.
	 * \return The string representation.
	 */
	virtual std::string toString() const = 0;
};

}

#endif

