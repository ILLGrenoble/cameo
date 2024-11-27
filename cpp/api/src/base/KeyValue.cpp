/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
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