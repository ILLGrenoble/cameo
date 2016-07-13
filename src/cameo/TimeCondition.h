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

#ifndef CAMEO_TIMECONDITION_H_
#define CAMEO_TIMECONDITION_H_

#include <boost/thread/thread.hpp>
#include <boost/thread/condition.hpp>

namespace cameo {

/**
 * An "improved" "one-time notify" condition. If a notify occurs before the wait then
 * the condition does NOT wait. Must not be used if the notify can occur for many times.
 */
class TimeCondition {

public:
	/**
	 * Constructor.
	 */
	TimeCondition();

	/**
	 * Destructor.
	 */
	~TimeCondition();

	/**
	 * Waits for a notification. Returns false if the time has been reached. True if notified.
	 */
	bool wait(long timeMs);

		/**
	 * Notifies the condition.
	 */
	void notify();

private:
	boost::mutex m_conditionMutex;
	boost::condition m_condition;
	bool m_notified;
};

}

#endif

