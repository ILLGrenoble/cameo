/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

#include "WaitingSet.h"
#include "Waiting.h"

using namespace std;

namespace cameo {

WaitingSet::WaitingSet() {

}

void WaitingSet::add(Waiting * waiting) {

	lock_guard<mutex> lock {m_mutex};

	m_set.insert(waiting);
}

void WaitingSet::remove(Waiting * waiting) {

	lock_guard<mutex> lock {m_mutex};

	set<Waiting *>::iterator it = m_set.find(waiting);

	if (it != m_set.end()) {
		m_set.erase(it);
	}
}

void WaitingSet::cancelAll() {

	lock_guard<mutex> lock {m_mutex};

	for (set<Waiting *>::iterator it = m_set.begin(); it != m_set.end(); ++it) {
		(*it)->cancel();
	}

}

}