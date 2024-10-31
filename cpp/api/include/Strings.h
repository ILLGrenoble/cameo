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

#include "Defines.h"
#include <string>
#include <vector>

namespace cameo {

namespace json {
	class StringObject;
}

const int Null = 0;

/**
 * Splits the string with delimiter.
 * \return The list of strings.
 */
CAMEO_EXPORT std::vector<std::string> split(const std::string& str, char c);

/**
 * Class defining an endpoint.
 */
class CAMEO_EXPORT Endpoint {

	friend std::ostream& operator<<(std::ostream&, const Endpoint&);

public:
	/**
	 * Constructor.
	 * \param protocol The protocol.
	 * \param address The address, can be an IP or a hostname.
	 * \param port The port.
	 */
	Endpoint(const std::string& protocol, const std::string& address, int port);

	/**
	 * Constructor with default tcp protocol.
	 * \param address The address, can be an IP or a hostname.
	 * \param port The port.
	 */
	Endpoint(const std::string& address, int port);

	/**
	 * Constructor.
	 */
	Endpoint();

	/**
	 * Equals operator.
	 * \param endpoint The endpoint to compare.
	 * \return True if equals, false otherwise.
	 */
	bool operator==(const Endpoint& endpoint) const;

	/**
	 * Non-equals operator.
	 * \param endpoint The endpoint to compare.
	 * \return True if equals, false otherwise.
	 */
	bool operator!=(const Endpoint& endpoint) const;

	/**
	 * Gets the protocol.
	 * \return The protocol.
	 */
	const std::string& getProtocol() const;

	/**
	 * Gets the address.
	 * \return The address.
	 */
	const std::string& getAddress() const;

	/**
	 * Gets the port.
	 * \return The port.
	 */
	int getPort() const;

	/**
	 * Parses the string into Endpoint.
	 * \return The parsed endpoint.
	 */
	static Endpoint parse(const std::string& str);

	/**
	 * Returns the endpoint by replacing the port.
	 * \return The endpoint with same protocol and address but with the port.
	 */
	Endpoint withPort(int port) const;

	/**
	 * Returns a string representation of this endpoint.
	 * \return The string representation.
	 */
	std::string toString() const;

private:
	std::string m_protocol;
	std::string m_address;
	int m_port;
};

/**
 * Class defining an application identity.
 */
class CAMEO_EXPORT ApplicationIdentity {

public:
	/**
	 * Constructor.
	 * \param name The name.
	 * \param id The id.
	 * \param endpoint The endpoint.
	 */
	ApplicationIdentity(const std::string& name, int id, const Endpoint& endpoint);

	/**
	 * Constructor.
	 * \param name The name.
	 * \param endpoint The endpoint.
	 */
	ApplicationIdentity(const std::string& name, const Endpoint& endpoint);

	/**
	 * Constructor.
	 */
	ApplicationIdentity();

	/**
	 * Gets the name.
	 * \return The name.
	 */
	const std::string& getName() const;

	/**
	 * Gets the id.
	 * \return The id.
	 */
	int getId() const;

	/**
	 * Gets the endpoint.
	 * \return The endpoint.
	 */
	const Endpoint& getEndpoint() const;

	/**
	 * adds this object to a json object.
	 * \param jsonObject A json::StringObject object.
	 */
	void toJSON(json::StringObject& jsonObject) const;

	/**
	 * Returns the JSON string representation.
	 * \return The JSON string representation.
	 */
	std::string toJSONString() const;

private:
	std::string m_name;
	int m_id;
	Endpoint m_endpoint;
};

/**
 * Class defining an application identity with starter.
 */
class CAMEO_EXPORT ApplicationWithStarterIdentity {

public:
	/**
	 * Constructor.
	 * \param application The application.
	 * \param starter The starter application.
	 */
	ApplicationWithStarterIdentity(const ApplicationIdentity& application, const ApplicationIdentity& starter);

	/**
	 * Constructor.
	 * \param application The application.
	 */
	ApplicationWithStarterIdentity(const ApplicationIdentity& application);

	/**
	 * Gets the application.
	 * \return The application identity.
	 */
	const ApplicationIdentity& getApplication() const;

	/**
	 * Returns true if starter exists.
	 * \return True if the starter exists.
	 */
	bool hasStarter() const;

	/**
	 * Gets the starter application.
	 * \return The starter application.
	 */
	const ApplicationIdentity& getStarter() const;

	/**
	 * adds this object to a json object.
	 * \param jsonObject A json::StringObject object.
	 */
	void toJSON(json::StringObject& jsonObject) const;

	/**
	 * Returns the JSON string representation.
	 * \return The JSON string representation.
	 */
	std::string toJSONString() const;

private:
	ApplicationIdentity m_application;
	bool m_hasStarter;
	ApplicationIdentity m_starter;
};

/**
 * Class defining a Server identity.
 */
class CAMEO_EXPORT ServerIdentity {

public:
	constexpr static const char* ENDPOINT = "endpoint";
	constexpr static const char* PROXY = "proxy";

	/**
	 * Constructor.
	 * \param endpoint The endpoint.
	 * \param proxy The proxy.
	 */
	ServerIdentity(const std::string& endpoint, bool proxy);

	/**
	 * adds this object to a json object.
	 * \param jsonObject A json::StringObject object.
	 */
	void toJSON(json::StringObject& jsonObject) const;

	/**
	 * Returns the JSON string representation.
	 * \return The JSON string representation.
	 */
	std::string toJSONString() const;

private:
	std::string m_endpoint;
	bool m_proxy;
};

/**
 * Class defining an App identity.
 */
class CAMEO_EXPORT AppIdentity {

public:
	constexpr static const char* NAME = "name";
	constexpr static const char* ID = "id";
	constexpr static const char* SERVER = "server";

	/**
	 * Constructor.
	 * \param name The name.
	 * \param id The id.
	 * \param server The server identity.
	 */
	AppIdentity(const std::string& name, int id, const ServerIdentity& server);

	/**
	 * adds this object to a json object.
	 * \param jsonObject A json::StringObject object.
	 */
	void toJSON(json::StringObject& jsonObject) const;

	/**
	 * Returns the JSON string representation.
	 * \return The JSON string representation.
	 */
	std::string toJSONString() const;

private:
	std::string m_name;
	int m_id;
	ServerIdentity m_server;
};

/**
 * Class defining a string id for the communication identities.
 */
struct CAMEO_EXPORT StringId {

	/**
	 * Creates the string id.
	 * \param name The name.
	 * \param id The integer id.
	 * \return The composed string id.
	 */
	static std::string from(const std::string& name, int id);

	/**
	 * Creates the string id.
	 * \param id The first name.
	 * \param name The second name.
	 * \return The composed string id.
	 */
	static std::string from(const std::string& name1, const std::string& name2);
};

}

/**
 * Stream operator for an Endpoint object.
 */
CAMEO_EXPORT std::ostream& operator<<(std::ostream& os, const cameo::Endpoint& endpoint);

#endif
