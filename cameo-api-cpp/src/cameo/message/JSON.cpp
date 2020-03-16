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

#include "JSON.h"

namespace cameo {

JSONObject::JSONObject() :
	m_writer(m_buffer) {
	m_writer.StartObject();
}

void JSONObject::push(const std::string& key, int64_t value) {
	m_writer.Key(key.c_str());
	m_writer.Int64(value);
}

void JSONObject::push(const std::string& key, bool value) {
	m_writer.Key(key.c_str());
	m_writer.Bool(value);
}

void JSONObject::push(const std::string& key, double value) {
	m_writer.Key(key.c_str());
	m_writer.Double(value);
}

void JSONObject::push(const std::string& key, const std::string& value) {
	m_writer.Key(key.c_str());
	m_writer.String(value.c_str());
}

void JSONObject::startObject() {
	m_writer.StartObject();
}

void JSONObject::endObject() {
	m_writer.EndObject();
}

void JSONObject::startArray() {
	m_writer.StartArray();
}

void JSONObject::endArray() {
	m_writer.EndArray();
}

std::string JSONObject::toString() {
	m_writer.EndObject();
	return m_buffer.GetString();
}

}
