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

/**
 * Namespace for the JSON encoding.
 */
namespace json {

/**
 * Helper class wrapping the rapidjson writer.
 */
class StringObject {

public:
	/**
	 * Constructor.
	 */
	StringObject();

	/**
	 * Pushes the key.
	 * \param key The key.
	 */
	void pushKey(const char* key);

	/**
	 * Pushes the key.
	 * \param key The key.
	 */
	void pushKey(const std::string& key);

	/**
	 * Pushes null.
	 */
	void pushNull();

	/**
	 * Pushes the value.
	 * \param value The value.
	 */
	void pushValue(int value);

	/**
	 * Pushes the value.
	 * \param value The value.
	 */
	void pushValue(int64_t value);

	/**
	 * Pushes the value.
	 * \param value The value.
	 */
	void pushValue(bool value);

	/**
	 * Pushes the value.
	 * \param value The value.
	 */
	void pushValue(double value);

	/**
	 * Pushes the value.
	 * \param value The value.
	 */
	void pushValue(const std::string& value);

	/**
	 * Starts an object.
	 */
	void startObject();

	/**
	 * Ends an object.
	 */
	void endObject();

	/**
	 * Starts an array.
	 */
	void startArray();

	/**
	 * Ends an object.
	 */
	void endArray();

	/**
	 * Returns the string representation of the JSON object.
	 * The JSON object is ended and dump() cannot be recalled.
	 * \return The string representation.
	 */
	std::string dump();

private:
	rapidjson::StringBuffer m_buffer;
	rapidjson::Writer<rapidjson::StringBuffer> m_writer;
};

typedef rapidjson::Document Object;
typedef rapidjson::Value Value;

template<typename Message>
bool parse(Object & object, const Message& message) {

	rapidjson::ParseResult ok = object.Parse(static_cast<const char *>(message.data()), message.size());
	if (!ok) {
		return false;
	}
	return true;
}

bool parse(Object & object, const std::string& string);

}
}

#endif
