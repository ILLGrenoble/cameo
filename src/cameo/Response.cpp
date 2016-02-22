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

#include "Response.h"

namespace cameo {

Response::Response(int value, const std::string& message) :
	m_value(value),
	m_message(message) {
}

Response::Response() :
	m_value(0) {
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

std::ostream& operator<<(std::ostream& os, const cameo::Response& response) {
	os << "value=" << response.m_value << "\nmessage=" << response.m_message;
	return os;
}

}