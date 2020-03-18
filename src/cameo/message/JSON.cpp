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
#include <iostream>

using namespace std;

namespace cameo {
namespace json {

StringObject::StringObject() :
	m_writer(m_buffer) {
	m_writer.StartObject();
}

void StringObject::pushKey(const char* key) {
	m_writer.Key(key);
}

void StringObject::pushInt(int value) {
	m_writer.Int(value);
}

void StringObject::pushInt64(int64_t value) {
	m_writer.Int64(value);
}

void StringObject::pushBool(bool value) {
	m_writer.Bool(value);
}

void StringObject::pushDouble(double value) {
	m_writer.Double(value);
}

void StringObject::pushString(const std::string& value) {
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

std::string StringObject::toString() {
	m_writer.EndObject();
	return m_buffer.GetString();
}

void parse(Object & object, zmq::message_t * message) {

	rapidjson::ParseResult ok = object.Parse(static_cast<char *>(message->data()), message->size());
	if (!ok) {
		cerr << "Cannot parse message" << endl;
	}
}

}
}
