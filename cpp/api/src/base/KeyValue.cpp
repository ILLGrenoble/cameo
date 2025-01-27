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

#include "KeyValue.h"

namespace cameo {

KeyValue::KeyValue(const std::string& key) :
	m_status{Status::UNDEFINED},
	m_key{key} {
}

void KeyValue::setStatus(Status status) {
	m_status = status;
}

void KeyValue::setValue(const std::string& value) {
	m_value = value;
}

KeyValue::Status KeyValue::getStatus() const {
	return m_status;
}

const std::string& KeyValue::getKey() const {
	return m_key;
}

const std::string& KeyValue::getValue() const {
	return m_value;
}

}
