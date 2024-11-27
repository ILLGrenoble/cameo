/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

#include "Object.h"

namespace cameo {

Object::Object() {
}

bool Object::isReady() const {
	return m_state == InternalState::READY;
}

bool Object::isTerminated() const {
	return m_state == InternalState::TERMINATED;
}

void Object::setReady() {
	m_state = InternalState::READY;
}

void Object::setTerminated() {
	m_state = InternalState::TERMINATED;
}

}