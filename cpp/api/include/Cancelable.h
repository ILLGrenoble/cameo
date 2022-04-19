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

#ifndef CAMEO_CANCELABLE_H_
#define CAMEO_CANCELABLE_H_

namespace cameo {

/**
 * Class defining an interface for cancelable objects.
 */
class Cancelable {

public:
	/**
	 * Destructor.
	 */
	virtual ~Cancelable() {}

	/**
	 * Cancels the object.
	 */
	virtual void cancel() = 0;

	/**
	 * Returns true if is canceled.
	 * \return True if is canceled.
	 */
	virtual bool isCanceled() const = 0;
};

}

#endif

