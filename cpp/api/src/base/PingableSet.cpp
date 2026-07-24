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
#include "PingableObject.h"
#include <iostream>

namespace cameo {

PingableSet::PingableSet() {
}

void PingableSet::add(PingableObject * object) {

	std::lock_guard<std::mutex> lock {m_mutex};

	m_set.insert(object);

	//std::cout << "Inserted " << object->toString() << std::endl;
}

void PingableSet::remove(PingableObject * object) {

	std::lock_guard<std::mutex> lock {m_mutex};

	//std::cout << "Removing " << object->toString() << std::endl;

	std::set<PingableObject *>::iterator it = m_set.find(object);

	if (it != m_set.end()) {
		m_set.erase(it);

		//std::cout << "Removed " << object->toString() << std::endl;
	}
}

void PingableSet::pingAll(int timeout) {

	//std::cout << "Ping all " << timeout << std::endl;

	std::lock_guard<std::mutex> lock {m_mutex};

	for (std::set<PingableObject *>::iterator it = m_set.begin(); it != m_set.end(); ++it) {
		if ((*it)->isPinged()) {
			//std::cout << "Pinging " << (*it)->toString() << std::endl;
			bool pong = (*it)->ping(timeout);
//			if (pong) {
//				std::cout << "Pong " << (*it)->toString() << std::endl;
//			}
//			else {
//				std::cout << "No pong " << (*it)->toString() << std::endl;
//			}
		}
	}
}

}
