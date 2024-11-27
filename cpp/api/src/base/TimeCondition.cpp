/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

#include "TimeCondition.h"

#include <chrono>
#include <iostream>

namespace cameo {
	
TimeCondition::TimeCondition() :
	m_notified{false} {
}

TimeCondition::~TimeCondition() {
}
	
bool TimeCondition::wait(long timeMs) {

	std::unique_lock<std::mutex> lock {m_conditionMutex};

	if (!m_notified) {
		// Return true if the notification is occurring before the timeout.
		return (m_condition.wait_for(lock, std::chrono::milliseconds{timeMs}) == std::cv_status::no_timeout);
	}
	
	// The notification has occurred.
	return true;
}

void TimeCondition::notify() {

	std::unique_lock<std::mutex> lock {m_conditionMutex};

	m_notified = true;	
	m_condition.notify_one();
}

}