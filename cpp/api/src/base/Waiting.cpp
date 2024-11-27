/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

#include "Waiting.h"
#include "WaitingSet.h"
#include "Application.h"
#include "This.h"
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