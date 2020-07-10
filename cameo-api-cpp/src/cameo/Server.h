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
#include <mutex>
#include "Application.h"
#include "ConnectionChecker.h"
#include "ConnectionTimeout.h"
#include "Response.h"
#include "Services.h"
#include "SubscriberCreationException.h"

namespace cameo {

namespace application {
	class This;
}

class EventListener;
class EventThread;

class Server : private Services {

	friend class SubscriberImpl;
	friend class RequestImpl;
	friend class application::Instance;
	friend class application::This;
	friend class application::Subscriber;
	friend std::ostream& operator<<(std::ostream&, const Server&);

public:
	typedef std::function<void (bool)> ConnectionCheckerType;

	Server(const std::string& endpoint, int timeoutMs = 0);
	~Server();

	void setTimeout(int timeoutMs);

	int getTimeout() const;
	const std::string& getEndpoint() const;
	const std::string& getUrl() const;
	int getPort() const;
	bool isAvailable(int timeoutMs) const;

	/**
	 * Returns true if is available. Uses the timeout if set or 10000ms.
	 */
	bool isAvailable() const;

	std::unique_ptr<application::Instance> start(const std::string& name, const std::vector<std::string> &args, Option options = NONE);
	std::unique_ptr<application::Instance> start(const std::string& name, Option options = NONE);
	application::InstanceArray connectAll(const std::string& name, Option options = NONE);
	std::unique_ptr<application::Instance> connect(const std::string& name, Option options = NONE);

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
	std::unique_ptr<EventStreamSocket> openEventStream();

	/**
	 * Creates a connection handler with polling time.
	 */
	std::unique_ptr<ConnectionChecker> createConnectionChecker(ConnectionCheckerType handler, int pollingTimeMs = 10000);

	/**
	 * Gets the event listeners. Copies the list.
	 */
	std::vector<EventListener *> getEventListeners();

	/**
	 * Registers an event listener.
	 */
	void registerEventListener(EventListener * listener);

	/**
	 * Unregisters an event listener.
	 */
	void unregisterEventListener(EventListener * listener);

private:
	std::unique_ptr<application::Instance> makeInstance();
	bool isAlive(int id) const;
	Response stopApplicationAsynchronously(int id, bool immediately) const;
	std::unique_ptr<application::Subscriber> createSubscriber(int id, const std::string& publisherName, const std::string& instanceName);
	int getAvailableTimeout() const;
	int getStreamPort(const std::string& name);

	std::mutex m_eventListenersMutex;
	std::vector<EventListener *> m_eventListeners;
	std::unique_ptr<EventThread> m_eventThread;
};

std::ostream& operator<<(std::ostream&, const Server&);

}

#endif
