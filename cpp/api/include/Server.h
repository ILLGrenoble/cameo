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
class ContextZmq;
class RequestSocket;

class Server {

	friend class application::Instance;
	friend class application::This;
	friend class EventStreamSocket;
	friend class OutputStreamSocket;
	friend std::ostream& operator<<(std::ostream&, const Server&);

public:
	typedef std::function<void (bool)> ConnectionCheckerType;

	Server(const Endpoint& endpoint, int timeoutMs = 0);
	Server(const std::string& endpoint, int timeoutMs = 0);
	~Server();

	void setTimeout(int value);

	int getTimeout() const;
	Endpoint getEndpoint() const;
	Endpoint getStatusEndpoint() const;
	std::array<int, 3> getVersion() const;
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
	void initServer(const Endpoint& endpoint, int timeoutMs);
	std::unique_ptr<application::Instance> makeInstance();
	bool isAlive(int id) const;

	Response stopApplicationAsynchronously(int id, bool immediately) const;
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
	void initStatus();
	int getStreamPort(const std::string& name);
	std::unique_ptr<OutputStreamSocket> createOutputStreamSocket(const std::string& name);
	std::unique_ptr<RequestSocket> createRequestSocket(const std::string& endpoint);
	std::unique_ptr<RequestSocket> createRequestSocket(const std::string& endpoint, int timeout);

	Endpoint m_serverEndpoint;
	int m_timeout;
	std::array<int, 3> m_serverVersion;
	int m_statusPort;
	std::unique_ptr<ContextZmq> m_contextImpl;
	std::unique_ptr<RequestSocket> m_requestSocket;

	std::mutex m_eventListenersMutex;
	std::vector<EventListener *> m_eventListeners;
	std::unique_ptr<EventThread> m_eventThread;
};

std::ostream& operator<<(std::ostream&, const Server&);

}

#endif