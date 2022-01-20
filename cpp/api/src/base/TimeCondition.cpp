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

#include "TimeCondition.h"

#include <chrono>
#include <iostream>

namespace cameo {
	
TimeCondition::TimeCondition() :
	m_notified(false) {
}

TimeCondition::~TimeCondition() {
}
	
bool TimeCondition::wait(long timeMs) {

	std::unique_lock<std::mutex> lock(m_conditionMutex);

	if (!m_notified) {
		// Return true if the notification is occurring before the timeout.
		return (m_condition.wait_for(lock, std::chrono::milliseconds(timeMs)) == std::cv_status::no_timeout);
	}
	
	// The notification has occurred.
	return true;
}

void TimeCondition::notify() {

	std::unique_lock<std::mutex> lock(m_conditionMutex);

	m_notified = true;	
	m_condition.notify_one();
}

}
