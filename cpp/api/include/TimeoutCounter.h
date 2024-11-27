/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

#ifndef CAMEO_TIMEOUTCOUNTER_H_
#define CAMEO_TIMEOUTCOUNTER_H_

#include "Defines.h"
#include <chrono>

namespace cameo {

/**
 * A class providing a helper to define timeouts for operations that have different steps.
 */
class CAMEO_EXPORT TimeoutCounter {

public:
	static const TimeoutCounter None;

	/**
	 * Constructor.
	 * \param value The time in milliseconds.
	 */
	TimeoutCounter(int value);

	/**
	 * Copy constructor.
	 * \param obj The object to copy.
	 */
	TimeoutCounter(const TimeoutCounter& obj);

	/**
	 * Returns the remaining time at this call.
	 */
	int remains() const;

private:
	int m_value;
	std::chrono::system_clock::time_point m_start;
};



}

#endif