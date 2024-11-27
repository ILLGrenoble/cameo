/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

#include "KeyEvent.h"
#include "JSON.h"
#include <iostream>

namespace cameo {

KeyEvent::KeyEvent(int id, const std::string& name, Status status, const std::string& key, const std::string& value) :
	Event{id, name},
	m_status{status},
	m_key{key},
	m_value{value} {
}

KeyEvent::KeyEvent(const KeyEvent& event) :
	Event{event},
	m_status{event.m_status},
	m_key{event.m_key},
	m_value{event.m_value} {
}

KeyEvent* KeyEvent::clone() {
	return new KeyEvent{*this};
}

KeyEvent::Status KeyEvent::getStatus() const {
	return m_status;
}

const std::string& KeyEvent::getKey() const {
	return m_key;
}

const std::string& KeyEvent::getValue() const {
	return m_value;
}

std::string KeyEvent::toString() const {
	json::StringObject jsonObject;

	jsonObject.pushKey("type");
	jsonObject.pushValue("key");

	jsonObject.pushKey("id");
	jsonObject.pushValue(m_id);

	jsonObject.pushKey("name");
	jsonObject.pushValue(m_name);

	jsonObject.pushKey("status");
	jsonObject.pushValue(m_status);

	jsonObject.pushKey("key");
	jsonObject.pushValue(m_key);

	jsonObject.pushKey("value");
	jsonObject.pushValue(m_value);

	return jsonObject.dump();
}

std::ostream& operator<<(std::ostream& os, const KeyEvent& event) {
	os << event.toString();

	return os;
}

}