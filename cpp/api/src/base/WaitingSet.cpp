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

#include "WaitingSet.h"
#include "Waiting.h"

using namespace std;

namespace cameo {

WaitingSet::WaitingSet() {

}

void WaitingSet::add(Waiting * waiting) {

	lock_guard<mutex> lock(m_mutex);

	m_set.insert(waiting);
}

void WaitingSet::remove(Waiting * waiting) {

	lock_guard<mutex> lock(m_mutex);

	set<Waiting *>::iterator it = m_set.find(waiting);

	if (it != m_set.end()) {
		m_set.erase(it);
	}
}

void WaitingSet::cancelAll() {

	lock_guard<mutex> lock(m_mutex);

	for (set<Waiting *>::iterator it = m_set.begin(); it != m_set.end(); ++it) {
		(*it)->cancel();
	}

}

}
