/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

#ifndef CAMEO_HEARTBEAT_H_
#define CAMEO_HEARTBEAT_H_

#include "Defines.h"
#include <mutex>
#include <condition_variable>
#include <thread>
#include <memory>

namespace cameo {

/**
 * Abstract class defining a heartbeat.
 */
class CAMEO_EXPORT Heartbeat {

public:
	/**
	 * Constructor.
	 */
	Heartbeat(int period);

	/**
	 * Destructor.
	 */
	virtual ~Heartbeat();

	/**
	 * Starts the heartbeat.
	 */
	void start();

	/**
	 * Terminates the heartbeat.
	 */
	void terminate();

	/**
	 * Pings all the objects.
	 */
	virtual void pingAll() = 0;

private:
	int m_period;
	std::mutex m_mutex;
	std::condition_variable m_pingCondition;
	std::unique_ptr<std::thread> m_pingThread;
};

}

#endif
