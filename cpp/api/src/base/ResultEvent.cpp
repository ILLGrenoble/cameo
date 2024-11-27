/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

#include "ResultEvent.h"
#include "JSON.h"
#include <iostream>

namespace cameo {

ResultEvent::ResultEvent(int id, const std::string& name, const std::string& data) :
	Event{id, name},
	m_data{data} {
}

ResultEvent::ResultEvent(const ResultEvent& event) :
	Event{event}, m_data{event.m_data} {
}

ResultEvent* ResultEvent::clone() {
	return new ResultEvent{*this};
}

const std::string& ResultEvent::getData() const {
	return m_data;
}

std::string ResultEvent::toString() const {
	json::StringObject jsonObject;

	jsonObject.pushKey("type");
	jsonObject.pushValue("result");

	jsonObject.pushKey("id");
	jsonObject.pushValue(m_id);

	jsonObject.pushKey("name");
	jsonObject.pushValue(m_name);

	return jsonObject.dump();
}

std::ostream& operator<<(std::ostream& os, const ResultEvent& event) {
	os << event.toString();

	return os;
}

}