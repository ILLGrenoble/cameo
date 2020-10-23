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

#ifndef CAMEO_STRINGS_H_
#define CAMEO_STRINGS_H_

#include <string>
#include <vector>
#include <optional>

namespace cameo {

struct Name {
	static bool check(const std::string& str);
};

std::vector<std::string> split(const std::string& str, char c);

class Endpoint {

public:
	Endpoint(const std::string& address, int port);

	const std::string& getAddress() const;
	int getPort() const;

	static Endpoint parse(const std::string& str);

	std::string toString() const;

private:
	std::string m_address;
	int m_port;
};


class NameId {

public:
	/**
	 * Constructor.
	 */
	NameId(const std::string& name);

	/**
	 * Constructor.
	 */
	NameId(const std::string& name, int id);

	const std::string& getName() const;

	/**
	 * Return the id which may not exist.
	 */
	const std::optional<int>& getId() const;

	static NameId parse(const std::string& str);

	std::string toString() const;

private:
	std::string m_name;
	std::optional<int> m_id;
};

class ApplicationIdentity {

public:
	ApplicationIdentity(const NameId& nameId, const Endpoint& endpoint);

	const NameId& getNameId() const;
	const Endpoint& getEndpoint() const;

	static ApplicationIdentity parse(const std::string& str);

	std::string toString() const;

private:
	NameId m_nameId;
	Endpoint m_endpoint;
};

class ApplicationAndStarterIdentities {

public:
	ApplicationAndStarterIdentities(const ApplicationIdentity& application);
	ApplicationAndStarterIdentities(const ApplicationIdentity& application, const ApplicationIdentity& starter);

	const ApplicationIdentity& getApplication() const;
	const std::optional<ApplicationIdentity>& getStarter() const;

	static ApplicationAndStarterIdentities parse(const std::string& str);

	std::string toString() const;

private:
	ApplicationIdentity m_application;
	std::optional<ApplicationIdentity> m_starter;
};

}

#endif
