/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

#ifndef CAMEO_CANCELABLE_H_
#define CAMEO_CANCELABLE_H_

#include "Defines.h"

namespace cameo {

/**
 * Class defining an interface for cancelable objects.
 */
class CAMEO_EXPORT Cancelable {

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