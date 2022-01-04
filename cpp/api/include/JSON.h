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

#ifndef CAMEO_JSON_H_
#define CAMEO_JSON_H_

#include <rapidjson/stringbuffer.h>
#include <rapidjson/writer.h>
#include <rapidjson/document.h>
#include <string>
#include <cstdint>

namespace cameo {
namespace json {

/**
 * Helper class wrapping the rapidjson writer.
 */
class StringObject {

public:
	StringObject();

	void pushKey(const char* key);

	void pushInt(int value);
	void pushInt64(int64_t value);
	void pushBool(bool value);
	void pushDouble(double value);
	void pushString(const std::string& value);

	void startObject();
	void endObject();

	void startArray();
	void endArray();

	std::string toString();

private:
	rapidjson::StringBuffer m_buffer;
	rapidjson::Writer<rapidjson::StringBuffer> m_writer;
};

typedef rapidjson::Document Object;
typedef rapidjson::Value Value;

template<typename T>
bool parse(Object & object, T message);

template<typename Message>
bool parse(Object & object, Message * message) {

	rapidjson::ParseResult ok = object.Parse(static_cast<char *>(message->data()), message->size());
	if (!ok) {
		return false;
	}
	return true;
}

bool parse(Object & object, const std::string& string);

}
}

#endif
