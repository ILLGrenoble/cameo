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
#include <boost/date_time/posix_time/posix_time.hpp>
#include <iostream>

using namespace std;

namespace cameo {
	
TimeCondition::TimeCondition() :
	m_notified(false) {
}

TimeCondition::~TimeCondition() {
}
	
bool TimeCondition::wait(long timeMs) {

	boost::mutex::scoped_lock lock(m_conditionMutex);
	
	if (!m_notified) {
		boost::posix_time::time_duration duration = boost::posix_time::millisec(timeMs);

		return m_condition.timed_wait(lock, duration);
	}
	
	// sure that notify occurred
	return true;
}

void TimeCondition::notify() {

	boost::mutex::scoped_lock lock(m_conditionMutex);

	m_notified = true;	
	m_condition.notify_one();
}

}
