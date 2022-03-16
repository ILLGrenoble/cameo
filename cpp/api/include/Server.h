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

#include "Application.h"
#include "ConnectionChecker.h"
#include "ConnectionTimeout.h"
#include "Response.h"
#include "Strings.h"
#include "EventStreamSocket.h"
#include "OutputStreamSocket.h"
#include <vector>
#include <memory>
#include <mutex>
#include <array>

namespace cameo {

namespace application {
	class This;
}

class EventListener;
class EventThread;
class Context;
class RequestSocket;

class Server {

	friend class application::Instance;
	friend class application::This;
	friend class EventStreamSocket;
	friend class OutputStreamSocket;
	friend std::ostream& operator<<(std::ostream&, const Server&);

public:
	typedef std::function<void (bool)> ConnectionCheckerType;

	Server(const Endpoint& endpoint, int timeoutMs = 0, bool useProxy = false);
	Server(const std::string& endpoint, int timeoutMs = 0, bool useProxy = false);
	~Server();
	void terminate();

	void setTimeout(int value);

	int getTimeout() const;
	Endpoint getEndpoint() const;
	Endpoint getStatusEndpoint() const;
	std::array<int, 3> getVersion() const;
	bool usesProxy() const;

	bool isAvailable(int timeout) const;

	/**
	 * Returns true if is available. Uses the timeout if set or 10000ms.
	 */
	bool isAvailable() const;

	std::unique_ptr<application::Instance> start(const std::string& name, const std::vector<std::string> &args, int options = 0);
	std::unique_ptr<application::Instance> start(const std::string& name, int options = 0);
	application::InstanceArray connectAll(const std::string& name, int options = 0);
	std::unique_ptr<application::Instance> connect(const std::string& name, int options = 0);
	std::unique_ptr<application::Instance> connect(int id, int options = 0);

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
	std::vector<application::Port> getPorts() const;

	/**
	 * throws ConnectionTimeout
	 */
	application::State getActualState(int id) const;

	/**
	 * throws ConnectionTimeout
	 */
	std::set<application::State> getPastStates(int id) const;

	/**
	 * throws ConnectionTimeout
	 */
	std::unique_ptr<EventStreamSocket> createEventStreamSocket();

	/**
	 * Creates a connection handler with polling time.
	 */
	std::unique_ptr<ConnectionChecker> createConnectionChecker(ConnectionCheckerType handler, int pollingTimeMs = 10000);

	/**
	 * Class used for filtering events.
	 */
	class FilteredEventListener {

	public:
		FilteredEventListener(EventListener * listener, bool filtered) : m_listener(listener), m_filtered(filtered) {}

		EventListener * getListener() const;
		bool isFiltered() const;

	private:
		EventListener * m_listener;
		bool m_filtered;
	};

	/**
	 * Gets the event listeners. Copies the list.
	 */
	std::vector<FilteredEventListener> getEventListeners();

	/**
	 * Registers an event listener.
	 */
	void registerEventListener(EventListener * listener, bool filtered = true);

	/**
	 * Unregisters an event listener.
	 */
	void unregisterEventListener(EventListener * listener);

private:
	void initServer(const Endpoint& endpoint, int timeoutMs);
	int getResponderProxyPort() const;
	int getPublisherProxyPort() const;
	int getSubscriberProxyPort() const;

	std::unique_ptr<application::Instance> makeInstance();
	bool isAlive(int id) const;

	Response stop(int id, bool immediately) const;
	int getAvailableTimeout() const;

	void storeKeyValue(int id, const std::string& key, const std::string& value);
	std::string getKeyValue(int id, const std::string& key);
	void removeKey(int id, const std::string& key);

	int requestPort(int id);
	void setPortUnavailable(int id, int port);
	void releasePort(int id, int port);

	json::Object requestJSON(const std::string& request, int overrideTimeout = -1);
	json::Object requestJSON(const std::string& requestPart1, const std::string& requestPart2, int overrideTimeout = -1);

	void initContext();
	void initRequestSocket();

	void retrieveServerVersion();
	int retrieveStatusPort();
	int retrieveStreamPort(const std::string& name);
	int retrieveResponderProxyPort();
	int retrievePublisherProxyPort();
	int retrieveSubscriberProxyPort();
	std::unique_ptr<OutputStreamSocket> createOutputStreamSocket(const std::string& name);
	std::unique_ptr<RequestSocket> createRequestSocket(const std::string& endpoint, const std::string& responderIdentity);
	std::unique_ptr<RequestSocket> createRequestSocket(const std::string& endpoint, const std::string& responderIdentity, int timeout);
	std::unique_ptr<RequestSocket> createServerRequestSocket();

	Endpoint m_serverEndpoint;
	int m_timeout;
	bool m_useProxy;
	std::array<int, 3> m_serverVersion;
	int m_responderProxyPort;
	int m_publisherProxyPort;
	int m_subscriberProxyPort;
	int m_serverStatusPort;
	int m_statusPort;
	std::unique_ptr<Context> m_context;
	std::unique_ptr<RequestSocket> m_requestSocket;

	std::mutex m_eventListenersMutex;
	std::vector<FilteredEventListener> m_eventListeners;
	std::unique_ptr<EventThread> m_eventThread;

	const static std::string CAMEO_SERVER;
};

std::ostream& operator<<(std::ostream&, const Server&);

}

#endif
