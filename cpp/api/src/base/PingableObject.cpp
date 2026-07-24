/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

#include "PingableObject.h"
#include "PingableSet.h"
#include "This.h"

namespace cameo {

void PingableObject::init() {

	// Add the object in the pingable set if This exists.
	if (This::m_instance.m_inited) {
		This::m_instance.m_pingableSet->add(this);
	}
}

void PingableObject::terminate() {

	// Remove the object from the pingable set if This exists.
	if (This::m_instance.m_inited) {
		This::m_instance.m_pingableSet->remove(this);
	}
}

void PingableObject::setPinged(bool value) {
	m_enabled = value;
}

bool PingableObject::isPinged() const {
	return m_enabled;
}

}
