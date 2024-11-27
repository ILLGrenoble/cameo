/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

#ifndef CAMEO_WAITINGSET_H_
#define CAMEO_WAITINGSET_H_

#include <set>
#include <thread>
#include <mutex>

namespace cameo {

class Waiting;

/**
 * Class containing a set of Waiting objects.
 * It is protected with a mutex because the class must be thread-safe.
 */
class WaitingSet {

public:
	/**
	 * Constructor.
	 */
	WaitingSet();

	/**
	 * Adds a Waiting object.
	 * \param waiting The Waiting object.
	 */
	void add(Waiting * waiting);

	/**
	 * Removes a Waiting object.
	 * \param waiting The Waiting object.
	 */
	void remove(Waiting * waiting);

	/**
	 * Cancels all the Waiting objects.
	 */
	void cancelAll();

private:
	std::mutex m_mutex;
	std::set<Waiting *> m_set;
};

}

#endif