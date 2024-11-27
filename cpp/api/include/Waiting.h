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

#ifndef CAMEO_WAITING_H_
#define CAMEO_WAITING_H_

#include "Defines.h"
#include <functional>

namespace cameo {

/**
 * Class defining a waiting that can be canceled. It is used in blocking calls.
 */
class CAMEO_EXPORT Waiting {

public:
	/**
	 * Type of the cancel function.
	 */
	typedef std::function<void ()> Function;

	/**
	 * Constructor.
	 * \param cancelFunction The cancel function.
	 */
	Waiting(Function cancelFunction);

	/**
	 * Destructor.
	 */
	~Waiting();

	/**
	 * Cancels the waiting by calling the cancel function.
	 */
	void cancel();

private:
	Function m_function;
};

}

#endif
