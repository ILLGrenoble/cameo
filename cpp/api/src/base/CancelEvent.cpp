/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

#include "CancelEvent.h"
#include "JSON.h"
#include <iostream>

namespace cameo {

CancelEvent::CancelEvent(int id, const std::string& name) :
	Event(id, name) {
}

CancelEvent::CancelEvent(const CancelEvent& event) :
	Event(event) {
}

CancelEvent* CancelEvent::clone() {
	return new CancelEvent(*this);
}

std::string CancelEvent::toString() const {
	json::StringObject jsonObject;

	jsonObject.pushKey("type");
	jsonObject.pushValue("cancel");

	jsonObject.pushKey("id");
	jsonObject.pushValue(m_id);

	jsonObject.pushKey("name");
	jsonObject.pushValue(m_name);

	return jsonObject.dump();
}

std::ostream& operator<<(std::ostream& os, const cameo::CancelEvent& event) {
	os << event.toString();

	return os;
}

}