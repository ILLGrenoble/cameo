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

#include "CancelIdGenerator.h"

using namespace std;

namespace cameo {

boost::mutex CancelIdGenerator::m_mutex;
int CancelIdGenerator::m_currentId = 0;

int CancelIdGenerator::newId() {

	boost::mutex::scoped_lock lock(m_mutex);
	m_currentId++;

	return m_currentId;
}

}