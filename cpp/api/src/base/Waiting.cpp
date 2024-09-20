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

#include "Waiting.h"
#include "WaitingSet.h"
#include "Application.h"

#include <iostream>

using namespace std;

namespace cameo {

Waiting::Waiting(Waiting::Function function)
: m_function{function} {

	// Add the object in the waiting set if This exists.
	if (This::m_instance.m_inited) {
		This::m_instance.m_waitingSet->add(this);
	}
}

Waiting::~Waiting() {

	// Remove the object in the waiting set if This exists.
	if (This::m_instance.m_inited) {
		This::m_instance.m_waitingSet->remove(this);
	}
}

void Waiting::cancel() {
	m_function();
}

}
