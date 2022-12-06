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

#include "TimeoutCounter.h"

#include <iostream>

namespace cameo {

const TimeoutCounter TimeoutCounter::None = TimeoutCounter(-1);

TimeoutCounter::TimeoutCounter(int value) :
	m_value(value) {

	m_start = std::chrono::system_clock::now();
}

TimeoutCounter::TimeoutCounter(const TimeoutCounter& obj) :
	m_value(obj.m_value), m_start(obj.m_start) {
}

int TimeoutCounter::remains() const {

	// Return no timeout if it is the case.
	if (m_value == -1) {
		return -1;
	}

	// Get the time elapsed since the creation of the object.
	std::chrono::duration<double> diff = std::chrono::system_clock::now() - m_start;

	// Return the time in milliseconds.
	int diffMs = diff.count() * 1000;

	if (diffMs > m_value) {
		return 0;
	}

	return m_value - diffMs;
}

}
