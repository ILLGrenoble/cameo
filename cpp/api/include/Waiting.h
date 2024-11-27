/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
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