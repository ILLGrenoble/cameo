/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
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