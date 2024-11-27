/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

#include "Response.h"
#include "JSON.h"

namespace cameo {

Response::Response(int value, const std::string& message) :
	m_value{value},
	m_message{message} {
}

Response::Response() :
	m_value{0} {
}

int Response::getValue() const {
	return m_value;
}

const std::string& Response::getMessage() const {
	return m_message;
}

bool Response::isSuccess() const {
	return (m_value != -1);
}

std::string Response::toString() const {
	json::StringObject jsonObject;

	jsonObject.pushKey("value");
	jsonObject.pushValue(m_value);

	jsonObject.pushKey("message");
	jsonObject.pushValue(m_message);

	return jsonObject.dump();
}

std::ostream& operator<<(std::ostream& os, const cameo::Response& response) {
	os << response.toString();

	return os;
}

}