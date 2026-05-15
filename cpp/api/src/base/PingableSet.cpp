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

namespace cameo {

PingableSet::PingableSet() {
}

void PingableSet::add(Pingable * pingable) {

	std::lock_guard<std::mutex> lock {m_mutex};

	m_set.insert(pingable);
}

void PingableSet::remove(Pingable * pingable) {

	std::lock_guard<std::mutex> lock {m_mutex};

	std::set<Pingable *>::iterator it = m_set.find(pingable);

	if (it != m_set.end()) {
		m_set.erase(it);
	}
}

void PingableSet::pingAll() {

	std::lock_guard<std::mutex> lock {m_mutex};

	for (std::set<Pingable *>::iterator it = m_set.begin(); it != m_set.end(); ++it) {
		(*it)->ping();
	}

}

}
