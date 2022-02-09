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

namespace cameo {

const int Null = 0;

std::vector<std::string> split(const std::string& str, char c);

class Endpoint {

	friend std::ostream& operator<<(std::ostream&, const Endpoint&);

public:
	Endpoint(const std::string& protocol, const std::string& address, int port);
	Endpoint(const std::string& address, int port);
	Endpoint();

	bool operator==(const Endpoint& endpoint) const;

	const std::string& getProtocol() const;
	const std::string& getAddress() const;
	int getPort() const;

	static Endpoint parse(const std::string& str);

	Endpoint withPort(int port) const;

	std::string toString() const;

private:
	std::string m_protocol;
	std::string m_address;
	int m_port;
};

class ApplicationIdentity {

public:
	ApplicationIdentity(const std::string& name, int id, const Endpoint& endpoint);
	ApplicationIdentity(const std::string& name, const Endpoint& endpoint);
	ApplicationIdentity();

	const std::string& getName() const;
	int getId() const;
	const Endpoint& getEndpoint() const;

	std::string toJSONString() const;

private:
	std::string m_name;
	int m_id;
	Endpoint m_endpoint;
};

class ApplicationWithStarterIdentity {

public:
	ApplicationWithStarterIdentity(const ApplicationIdentity& application, const ApplicationIdentity& starter);
	ApplicationWithStarterIdentity(const ApplicationIdentity& application);

	const ApplicationIdentity& getApplication() const;
	bool hasStarter() const;
	const ApplicationIdentity& getStarter() const;

	std::string toJSONString() const;

private:
	ApplicationIdentity m_application;
	bool m_hasStarter;
	ApplicationIdentity m_starter;
};

struct ResponderIdentity {

	static std::string from(int id, const std::string& responderName);
};

}

std::ostream& operator<<(std::ostream& os, const cameo::Endpoint& endpoint);

#endif
