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

#ifndef CAMEO_TIMEOUTCOUNTER_H_
#define CAMEO_TIMEOUTCOUNTER_H_

#include <chrono>

namespace cameo {

/**
 * A class providing a helper to define timeouts for operations that have different steps.
 */
class TimeoutCounter {

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

