/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

#ifndef CAMEO_PINGABLESET_H_
#define CAMEO_PINGABLESET_H_

#include <set>
#include <mutex>

namespace cameo {

class Pingable;

/**
 * Class containing a set of Pingable objects.
 * It is protected with a mutex because the class must be thread-safe.
 */
class PingableSet {

public:
	/**
	 * Constructor.
	 */
	PingableSet();

	/**
	 * Adds a Pingable object.
	 * \param Pingable The Pingable object.
	 */
	void add(Pingable * Pingable);

	/**
	 * Removes a Pingable object.
	 * \param Pingable The Pingable object.
	 */
	void remove(Pingable * Pingable);

	/**
	 * Pings all the Pingable objects.
	 */
	void pingAll();

private:
	std::mutex m_mutex;
	std::set<Pingable *> m_set;
};

}

#endif
