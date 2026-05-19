/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

#include "PingableSet.h"
#include "Pingable.h"
#include <iostream>

namespace cameo {

PingableSet::PingableSet() {
}

void PingableSet::add(Pingable * pingable) {

	std::lock_guard<std::mutex> lock {m_mutex};

	m_set.insert(pingable);

	//std::cout << "Inserted " << pingable << std::endl;
}

void PingableSet::remove(Pingable * pingable) {

	std::lock_guard<std::mutex> lock {m_mutex};

	//std::cout << "Removing " << pingable << std::endl;

	std::set<Pingable *>::iterator it = m_set.find(pingable);

	if (it != m_set.end()) {
		m_set.erase(it);

		//std::cout << "Removed " << pingable << std::endl;
	}
}

void PingableSet::pingAll(int timeout) {

	std::lock_guard<std::mutex> lock {m_mutex};

	for (std::set<Pingable *>::iterator it = m_set.begin(); it != m_set.end(); ++it) {
		if ((*it)->isPinged()) {
			//std::cout << "Pinging " << *it << std::endl;
			bool pong = (*it)->ping(timeout);
//			if (pong) {
//				std::cout << "Pong " << *it << std::endl;
//			}
//			else {
//				std::cout << "No pong " << *it << std::endl;
//			}
		}
	}
}

}
