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

#ifndef CAMEO_SERVER_H_
#define CAMEO_SERVER_H_

#include <vector>
#include <memory>
#include "Application.h"
#include "ConnectionTimeout.h"
#include "Response.h"
#include "Services.h"
#include "SubscriberCreationException.h"

namespace cameo {

class ConnectionHandlerSet;

namespace application {
	class This;
}

class Server : private Services {

	friend class SubscriberImpl;
	friend class RequestImpl;
	friend class application::Instance;
	friend class application::This;
	friend class application::Subscriber;
	friend std::ostream& operator<<(std::ostream&, const Server&);

public:
	typedef boost::function<void (bool)> ConnectionHandlerType;

	Server(const std::string& endpoint);
	~Server();

	void setTimeout(int timeoutMs, int connectionPollingTimeMs);
	void setTimeout(int timeoutMs);

	int getTimeout() const;
	int getConnectionPollingTime() const;
	const std::string& getEndpoint() const;
	const std::string& getUrl() const;
	int getPort() const;
	bool isAvailable(int timeoutMs) const;

	/**
	 * Returns true if is available. Uses the timeout if set or 10000ms.
	 */
	bool isAvailable() const;

	std::auto_ptr<application::Instance> start(const std::string& name, const std::vector<std::string> &args, Option options = NONE);
	std::auto_ptr<application::Instance> start(const std::string& name, Option options = NONE);
	application::InstanceArray connectAll(const std::string& name);
	std::auto_ptr<application::Instance> connect(const std::string& name);

	/**
	 * throws ConnectionTimeout
	 */
	void killAllAndWaitFor(const std::string& name);

	/**
	 * throws ConnectionTimeout
	 */
	std::vector<application::Configuration> getApplicationConfigurations() const;

	/**
	 * throws ConnectionTimeout
	 */
	std::vector<application::Info> getApplicationInfos() const;

	/**
	 * throws ConnectionTimeout
	 */
	std::vector<application::Info> getApplicationInfos(const std::string& name) const;

	/**
	 * throws ConnectionTimeout
	 */
	std::auto_ptr<EventStreamSocket> openEventStream();

	/**
	 * Adds a connection handler.
	 */
	void addConnectionHandler(std::string const & name, ConnectionHandlerType handler);

	/**
	 * Removes a connection handler. Returns true if the handler is found and removed.
	 */
	bool removeConnectionHandler(std::string const & name);

private:
	std::auto_ptr<application::Instance> makeInstance();
	bool isAlive(int id) const;
	Response stopApplicationAsynchronously(int id, bool immediately) const;
	std::auto_ptr<application::Instance> stop(int id, bool immediately);
	std::auto_ptr<application::Subscriber> createSubscriber(int id, const std::string& publisherName, const std::string& instanceName) const;

	ServicesImpl * m_impl;
	int m_connectionPollingTimeMs;
	std::auto_ptr<ConnectionHandlerSet> m_connectionHandlerSet;
};

std::ostream& operator<<(std::ostream&, const Server&);

}

#endif
