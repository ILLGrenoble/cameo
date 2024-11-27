/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

#ifndef CAMEO_OBJECT_H_
#define CAMEO_OBJECT_H_

#include "Defines.h"
#include <string>
#include <atomic>

namespace cameo {

/**
 * Class defining an abstract Cameo object that can be initialized and terminated.
 */
class CAMEO_EXPORT Object {

public:
	/**
	 * Constructor.
	 */
	Object();

	/**
	 * Destructor.
	 */
	virtual ~Object() {}

	/**
	 * Initializes the object.
	 */
	virtual void init() = 0;

	/**
	 * Returns true if is ready.
	 * \return True if is ready.
	 */
	bool isReady() const;

	/**
	 * Terminates the object.
	 */
	virtual void terminate() = 0;

	/**
	 * Returns true if is terminated.
	 * \return True if is terminated.
	 */
	bool isTerminated() const;

	/**
	 * Returns a string representation of this application.
	 * \return The string representation.
	 */
	virtual std::string toString() const = 0;

protected:
	/**
	 * Sets the state ready.
	 */
	void setReady();

	/**
	 * Sets the state terminated.
	 */
	void setTerminated();

private:
	enum class InternalState {INIT, READY, TERMINATED};

	std::atomic<InternalState> m_state = InternalState::INIT;
};

}

#endif