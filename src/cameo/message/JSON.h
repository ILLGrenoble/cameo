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

#include <string>
#include <rapidjson/stringbuffer.h>
#include <rapidjson/prettywriter.h>
#include <stdint.h>
//#include <rapidjson/writer.h>

namespace cameo {

class JSONObject {

	JSONObject();

	void push(const std::string& key, int64_t value);
	void push(const std::string& key, bool value);
	void push(const std::string& key, double value);
	void push(const std::string& key, const std::string& value);

	void startObject();
	void endObject();

	void startArray();
	void endArray();

	std::string toString();

private:
	rapidjson::StringBuffer m_buffer;
	rapidjson::PrettyWriter<rapidjson::StringBuffer> m_writer;
};

}

#endif
