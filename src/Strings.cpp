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

bool Name::check(const std::string& str) {

	regex nameRegex("[a-zA-Z0-9\\-_]+");
	std::cmatch m;

	return regex_match(str.c_str(), m, nameRegex);
}

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

	if (str.substr(0, 6) != "tcp://") {
		throw new BadFormatException("Bad format for endpoint " + str);
	}

	string substr = str.substr(6);
	vector<string> tokens = split(substr, ':');

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
	return string("tcp://") + m_address + ":" + to_string(m_port);
}

NameId::NameId(const std::string& name, int id) :
	m_name(name),
	m_id(id) {
}

const std::string& NameId::getName() const {
	return m_name;
}

int NameId::getId() const {
	return m_id;
}

NameId NameId::parse(const std::string& str) {

	vector<string> tokens = split(str, '.');

	if (tokens.size() > 2) {
		throw BadFormatException("Bad format for nameid " + str);
	}

	string name = tokens[0];
	int id = -1;

	if (tokens.size() == 2) {
		try {
			id = stoi(tokens[1]);
		}
		catch (...) {
			throw new BadFormatException("Bad format for endpoint " + str);
		}
	}

	return NameId(name, id);
}

std::string NameId::toString() const {

	if (m_id > 0) {
		return m_name + "." + to_string(m_id);
	}
	return m_name;
}

ApplicationIdentity::ApplicationIdentity(const NameId& nameId, const Endpoint& endpoint) :
	m_nameId(nameId),
	m_endpoint(endpoint) {
}

const NameId& ApplicationIdentity::getNameId() const {
	return m_nameId;
}

const Endpoint& ApplicationIdentity::getEndpoint() const {
	return m_endpoint;
}

ApplicationIdentity ApplicationIdentity::parse(const std::string& str) {

	vector<string> tokens = split(str, '@');

	if (tokens.size() != 2) {
		throw BadFormatException("Bad format for application identity " + str);
	}

	return ApplicationIdentity(NameId::parse(tokens[0]), Endpoint::parse(tokens[1]));
}

std::string ApplicationIdentity::toString() const {
	return m_nameId.toString() + "@" + m_endpoint.toString();
}

ApplicationAndStarterIdentities::ApplicationAndStarterIdentities(const ApplicationIdentity& application, const ApplicationIdentity& starter) :
	m_application(application),
	m_starter(starter) {
}

const ApplicationIdentity& ApplicationAndStarterIdentities::getApplication() const {
	return m_application;
}

const ApplicationIdentity& ApplicationAndStarterIdentities::getStarter() const {
	return m_starter;
}

ApplicationAndStarterIdentities ApplicationAndStarterIdentities::parse(const std::string& str) {

	// The string is either <name>@<endpoint>:<name>@<endpoint> or <name>@<endpoint>:
	// To separate the two identities, we search for the last : before the last @.

	string::size_type firstIndex = str.find_first_of('@');

	if (firstIndex == string::npos) {
		throw new BadFormatException("Bad format for application and starter identities " + str);
	}

	string::size_type index = str.find_last_of('@');

	if (index == firstIndex) {

		// Format <name>@<endpoint>:
		if (str[str.length() - 1] != ':') {
			throw BadFormatException("Bad format for application and starter identities " + str);
		}

		string applicationString = str.substr(0, str.length() - 1);

		ApplicationIdentity application = ApplicationIdentity::parse(applicationString);

		return ApplicationAndStarterIdentities(application, application);
	}
	else {
		// Format <name>@<endpoint>:<name>@<endpoint>
		string substring = str.substr(0, index);
		index = substring.find_last_of(':');

		if (index == string::npos) {
			throw BadFormatException("Bad format for application and starter identities " + str);
		}

		string applicationString = str.substr(0, index);
		string starterString = str.substr(index + 1, str.length() - index);

		ApplicationIdentity application = ApplicationIdentity::parse(applicationString);
		ApplicationIdentity starter = ApplicationIdentity::parse(starterString);

		return ApplicationAndStarterIdentities(application, starter);
	}
}

std::string ApplicationAndStarterIdentities::toString() const {
//	if (starter != null) {
//		return application + ":" + starter;
//	}
//	return application + ":";

	return "";
}

}

