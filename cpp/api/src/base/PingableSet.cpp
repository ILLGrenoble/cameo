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
#include "Logger.h"
#include <iostream>

namespace cameo {

PingableSet::PingableSet() {
}

void PingableSet::add(PingableObject * object) {

	std::lock_guard<std::mutex> lock {m_mutex};

	m_set.insert(object);

	Logger::log(std::string{"Inserted"} + object->toString());
}

void PingableSet::remove(PingableObject * object) {

	std::lock_guard<std::mutex> lock {m_mutex};

	Logger::log(std::string{"Removing "} + object->toString());

	std::set<PingableObject *>::iterator it = m_set.find(object);

	if (it != m_set.end()) {
		m_set.erase(it);

		Logger::log(std::string{"Removed "} + object->toString());
	}
}

void PingableSet::pingAll(int timeout) {

	Logger::log(std::string{"Ping all "} + std::to_string(timeout));

	std::lock_guard<std::mutex> lock {m_mutex};

	for (std::set<PingableObject *>::iterator it = m_set.begin(); it != m_set.end(); ++it) {
		if ((*it)->isPinged()) {
			Logger::log(std::string{"Ping "} + (*it)->toString());
			bool pong = (*it)->ping(timeout);
			if (pong) {
				Logger::log(std::string{"Pong for "} + (*it)->toString());
			}
			else {
				Logger::log(std::string{"No pong for "} + (*it)->toString());
			}
		}
	}
}

}
