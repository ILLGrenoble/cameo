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
#include "Messages.h"
#include "BadFormatException.h"
#include "JSON.h"
#include <regex>
#include <ostream>
#include <iostream>

namespace cameo {

std::vector<std::string> split(const std::string& str, char c) {

	std::vector<std::string> result;

	std::string::size_type lastIndex {0};
	std::string::size_type index {str.find(c)};
	while (index != std::string::npos) {
		result.push_back(str.substr(lastIndex, index - lastIndex));
		lastIndex = index + 1;
		index = str.find(c, lastIndex);
	}
	result.push_back(str.substr(lastIndex));

	return result;
}

Endpoint::Endpoint(const std::string& protocol, const std::string& address, int port) :
	m_protocol{protocol},
	m_address{address},
	m_port{port} {
}

Endpoint::Endpoint(const std::string& address, int port) :
	m_protocol{"tcp"},
	m_address{address},
	m_port{port} {
}

Endpoint::Endpoint() :
	m_port{0} {
}

bool Endpoint::operator==(const Endpoint& endpoint) const {
	return m_protocol == endpoint.m_protocol
			&& m_address == endpoint.m_address
			&& m_port == endpoint.m_port;
}

bool Endpoint::operator!=(const Endpoint& endpoint) const {
	return !(*this == endpoint);
}

const std::string& Endpoint::getProtocol() const {
	return m_protocol;
}

const std::string& Endpoint::getAddress() const {
	return m_address;
}

int Endpoint::getPort() const {
	return m_port;
}

Endpoint Endpoint::parse(const std::string& str) {

	std::vector<std::string> tokens {split(str, ':')};

	if (tokens.size() != 3) {
		throw BadFormatException("Bad format for endpoint " + str);
	}

	std::string protocol {tokens[0]};
	std::string substr {tokens[1]};

	std::string address {substr.substr(2)};

	try {
		address = substr.substr(2);
	}
	catch (...) {
		throw BadFormatException("Bad format for endpoint " + str);
	}

	int port {0};

	try {
		port = stoi(tokens[2]);
	}
	catch (...) {
		throw BadFormatException("Bad format for endpoint " + str);
	}

	return Endpoint{protocol, address, port};
}

Endpoint Endpoint::withPort(int port) const {
	return Endpoint{m_protocol, m_address, port};
}

std::string Endpoint::toString() const {
	if (m_address != "") {
		return m_protocol + "://" + m_address + ":" + std::to_string(m_port);
	}
	return "";
}

ApplicationIdentity::ApplicationIdentity(const std::string& name, int id, const Endpoint& endpoint) :
	m_name{name},
	m_id{id},
	m_endpoint{endpoint} {
}

ApplicationIdentity::ApplicationIdentity(const std::string& name, const Endpoint& endpoint) :
	m_name{name},
	m_id{Null},
	m_endpoint{endpoint} {
}

ApplicationIdentity::ApplicationIdentity() :
	m_id{Null} {
}

const std::string& ApplicationIdentity::getName() const {
	return m_name;
}

int ApplicationIdentity::getId() const {
	return m_id;
}

const Endpoint& ApplicationIdentity::getEndpoint() const {
	return m_endpoint;
}

void ApplicationIdentity::toJSON(json::StringObject& jsonObject) const {

	jsonObject.pushKey(message::ApplicationIdentity::NAME);
	jsonObject.pushValue(m_name);

	if (m_id != Null) {
		jsonObject.pushKey(message::ApplicationIdentity::ID);
		jsonObject.pushValue(m_id);
	}

	jsonObject.pushKey(message::ApplicationIdentity::SERVER);
	jsonObject.pushValue(m_endpoint.toString());
}

std::string ApplicationIdentity::toJSONString() const {

	json::StringObject jsonObject;
	toJSON(jsonObject);

	return jsonObject.dump();
}

ApplicationWithStarterIdentity::ApplicationWithStarterIdentity(const ApplicationIdentity& application, const ApplicationIdentity& starter) :
	m_application{application},
	m_hasStarter{true},
	m_starter{starter} {
}

ApplicationWithStarterIdentity::ApplicationWithStarterIdentity(const ApplicationIdentity& application) :
	m_application{application},
	m_hasStarter{false} {
}

const ApplicationIdentity& ApplicationWithStarterIdentity::getApplication() const {
	return m_application;
}

bool ApplicationWithStarterIdentity::hasStarter() const {
	return m_hasStarter;
}

const ApplicationIdentity& ApplicationWithStarterIdentity::getStarter() const {
	return m_starter;
}

void ApplicationWithStarterIdentity::toJSON(json::StringObject& jsonObject) const {

	m_application.toJSON(jsonObject);

	if (m_hasStarter) {
		jsonObject.pushKey(message::ApplicationIdentity::STARTER);
		jsonObject.startObject();

		jsonObject.pushKey(message::ApplicationIdentity::NAME);
		jsonObject.pushValue(m_starter.getName());

		jsonObject.pushKey(message::ApplicationIdentity::ID);
		jsonObject.pushValue(m_starter.getId());

		jsonObject.pushKey(message::ApplicationIdentity::SERVER);
		jsonObject.pushValue(m_starter.getEndpoint().toString());

		jsonObject.endObject();
	}
}

std::string ApplicationWithStarterIdentity::toJSONString() const {

	json::StringObject jsonObject;
	toJSON(jsonObject);

	return jsonObject.dump();
}

ServerIdentity::ServerIdentity(const std::string& endpoint, bool proxy) :
	m_endpoint{endpoint},
	m_proxy{proxy} {
}

void ServerIdentity::toJSON(json::StringObject& jsonObject) const {

	jsonObject.pushKey(ENDPOINT);
	jsonObject.pushValue(m_endpoint);

	jsonObject.pushKey(PROXY);
	jsonObject.pushValue(m_proxy);
}

std::string ServerIdentity::toJSONString() const {

	json::StringObject jsonObject;
	toJSON(jsonObject);

	return jsonObject.dump();
}

AppIdentity::AppIdentity(const std::string& name, int id, const ServerIdentity& server) :
	m_name{name},
	m_id{id},
	m_server{server} {
}

void AppIdentity::toJSON(json::StringObject& jsonObject) const {

	jsonObject.pushKey(NAME);
	jsonObject.pushValue(m_name);

	jsonObject.pushKey(ID);
	jsonObject.pushValue(m_id);

	jsonObject.pushKey(SERVER);
	jsonObject.startObject();
	m_server.toJSON(jsonObject);
	jsonObject.endObject();
}

std::string AppIdentity::toJSONString() const {

	json::StringObject jsonObject;
	toJSON(jsonObject);

	return jsonObject.dump();
}


std::string StringId::from(const std::string& name, int id) {
	return name + ":" + std::to_string(id);
}

std::string StringId::from(const std::string& name1, const std::string& name2) {
	return name1 + ":" + name2;
}

std::ostream& operator<<(std::ostream& os, const cameo::Endpoint& endpoint) {

	os << endpoint.toString();

	return os;
}

}

