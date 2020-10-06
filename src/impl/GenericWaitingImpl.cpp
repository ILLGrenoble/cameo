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

#include "GenericWaitingImpl.h"

#include <iostream>

#include "Application.h"
#include "WaitingImplSet.h"

using namespace std;

namespace cameo {

GenericWaitingImpl::GenericWaitingImpl(GenericWaitingImpl::Function function)
: m_function(function) {

	// Add the object in the waiting set if This exists.
	if (application::This::m_instance.m_impl != nullptr) {
		application::This::m_instance.m_waitingSet->add(this);
	}
}

GenericWaitingImpl::~GenericWaitingImpl() {

	// Remove the object in the waiting set if This exists.
	if (application::This::m_instance.m_impl != nullptr) {
		application::This::m_instance.m_waitingSet->remove(this);
	}
}

void GenericWaitingImpl::cancel() {
	m_function();
}

}
