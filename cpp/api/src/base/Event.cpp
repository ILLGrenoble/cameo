/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

#include "Event.h"

namespace cameo {

Event::Event(int id, const std::string& name) :
	m_id{id},
	m_name{name} {
}

Event::Event(const Event& event) :
	m_id{event.m_id},
	m_name{event.m_name} {
}

Event::~Event() {
}

int Event::getId() const {
	return m_id;
}

const std::string& Event::getName() const {
	return m_name;
}

}