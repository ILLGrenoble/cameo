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

#include "Strings.h"
#include "BadFormatException.h"
#include <regex>

using namespace std;

namespace cameo {

std::vector<std::string> split(const std::string& str, char c) {

	vector<string> result;

	string::size_type lastIndex = 0;
	string::size_type index = str.find(c);
	while (index != string::npos) {
		result.push_back(str.substr(lastIndex, index - lastIndex));
		lastIndex = index + 1;
		index = str.find(c, lastIndex);
	}
	result.push_back(str.substr(lastIndex));

	return result;
}

Endpoint::Endpoint(const std::string& address, int port) {
	m_address = address;
	m_port = port;
}

const std::string& Endpoint::getAddress() const {
	return m_address;
}

int Endpoint::getPort() const {
	return m_port;
}

Endpoint Endpoint::parse(const std::string& str) {

	vector<string> tokens = split(str, ':');

	if (tokens.size() != 2) {
		throw new BadFormatException("Bad format for endpoint " + str);
	}

	string address = tokens[0];
	int port = 0;

	try {
		port = stoi(tokens[1]);
	}
	catch (...) {
		throw new BadFormatException("Bad format for endpoint " + str);
	}

	return Endpoint(address, port);
}

std::string Endpoint::toString() const {
	return m_address + ":" + to_string(m_port);
}

}

