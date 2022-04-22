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
