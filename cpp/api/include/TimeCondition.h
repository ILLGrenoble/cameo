/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

#ifndef CAMEO_TIMECONDITION_H_
#define CAMEO_TIMECONDITION_H_

#include "Defines.h"
#include <mutex>
#include <condition_variable>

namespace cameo {

/**
 * An "improved" "one-time notify" condition. If a notify occurs before the wait then
 * the condition does NOT wait. Must not be used if the notify can occur for many times.
 */
class CAMEO_EXPORT TimeCondition {

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
	 * \param timeMs The time in milliseconds.
	 * \return True if the notification has been done, false otherwise.
	 */
	bool wait(long timeMs);

	/**
	 * Notifies the condition.
	 */
	void notify();

private:
	std::mutex m_conditionMutex;
	std::condition_variable m_condition;
	bool m_notified;
};

}

#endif