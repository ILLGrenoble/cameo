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
namespace json {

StringObject::StringObject() :
	m_writer(m_buffer) {
	m_writer.StartObject();
}

void StringObject::pushKey(const char* key) {
	m_writer.Key(key);
}

void StringObject::pushKey(const std::string& key) {
	m_writer.Key(key.c_str());
}

void StringObject::pushNull() {
	m_writer.Null();
}

void StringObject::pushValue(int value) {
	m_writer.Int(value);
}

void StringObject::pushValue(int64_t value) {
	m_writer.Int64(value);
}

void StringObject::pushValue(bool value) {
	m_writer.Bool(value);
}

void StringObject::pushValue(double value) {
	m_writer.Double(value);
}

void StringObject::pushValue(const std::string& value) {
	m_writer.String(value.c_str());
}

void StringObject::startObject() {
	m_writer.StartObject();
}

void StringObject::endObject() {
	m_writer.EndObject();
}

void StringObject::startArray() {
	m_writer.StartArray();
}

void StringObject::endArray() {
	m_writer.EndArray();
}

std::string StringObject::dump() {
	m_writer.EndObject();
	return m_buffer.GetString();
}

bool parse(Object & object, const std::string& string) {

	rapidjson::ParseResult ok {object.Parse(static_cast<const char *>(string.c_str()), string.size())};
	if (!ok) {
		return false;
	}
	return true;
}

json::Object toJSON(const std::string& string) {

	json::Object response;
	json::parse(response, string);

	return response;
}

}
}
