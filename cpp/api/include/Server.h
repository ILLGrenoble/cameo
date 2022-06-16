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

class This;
class EventListener;
class EventThread;
class Context;
class RequestSocket;

/**
 * Class defining a Cameo remote server.
 * A Server object is not a server responding to requests but the representation of a remote Cameo server.
 */
class Server : public Object, public Timeoutable {

	friend class App;
	friend class This;
	friend class EventStreamSocket;
	friend class OutputStreamSocket;
	friend std::ostream& operator<<(std::ostream&, const Server&);

public:
	typedef std::function<void (bool)> ConnectionCheckerType;

	/**
	 * Constructor.
	 * \param endpoint The endpoint of the remote server.
	 * \param useProxy Uses the proxy or not.
	 */
	static std::unique_ptr<Server> create(const Endpoint& endpoint, bool useProxy = false);

	/**
	 * Constructor.
	 * \param endpoint The endpoint of the remote server.
	 * \param useProxy Uses the proxy or not.
	 */
	static std::unique_ptr<Server> create(const std::string& endpoint, bool useProxy = false);
	
	/**
	 * Destructor.
	 */
	~Server() override;

	/**
	 * Initializes the server.
	 * \throws InvalidArgumentException if the endpoint is not valid.
	 * \throws InitException if the server cannot be initialized.
	 * \throws ConnectionTimeout if the connection with the Cameo server fails.
	 */
	void init() override;

	/**
	 * Terminates the communications.
	 */
	void terminate() override;

	/**
	 * Sets the timeout.
	 * \param value The timeout.
	 */
	void setTimeout(int value) override;

	/**
	 * Gets the timeout.
	 * \return The timeout.
	 */
	int getTimeout() const override;

	/**
	 * Gets the endpoint of the server running this remote application.
	 * \return The endpoint.
	 */
	Endpoint getEndpoint() const;

	/**
	 * Gets the status endpoint of the server running this remote application.
	 * \return The endpoint.
	 */
	Endpoint getStatusEndpoint() const;

	/**
	 * Gets the version of the server running this remote application.
	 * \return The version.
	 */
	std::array<int, 3> getVersion() const;

	/**
	 * Returns the use of proxy.
	 * \return True if the proxy is used to access the remote Cameo server.
	 */
	bool usesProxy() const;

	/**
	 * Returns true if the remote server is available.
	 * \param timeout The timeout.
	 */
	bool isAvailable(int timeout) const;

	/**
	 * Returns true if is available. Uses the timeout if set or 10000ms.
	 * \return True if is available.
	 */
	bool isAvailable() const;

	/**
	 * Starts the application with name.
	 * \param name The name.
	 * \param args The arguments passed to the executable.
	 * \param options The options.
	 * \return The App object representing the remote application.
	 * \throws StartException when the application cannot be started.
	 */
	std::unique_ptr<App> start(const std::string& name, const std::vector<std::string> &args, int options = 0);

	/**
	 * Starts the application with name.
	 * \param name The name.
	 * \param options The options.
	 * \return The App object representing the remote application.
	 * \throws StartException when the application cannot be started.
	 */
	std::unique_ptr<App> start(const std::string& name, int options = 0);

	/**
	 * Connects to all the applications with name.
	 * \param name The name.
	 * \param options The options.
	 * \return The array of App objects representing the remote applications.
	 */
	AppArray connectAll(const std::string& name, int options = 0);

	/**
	 * Connects to an application with name.
	 * \param name The name.
	 * \param options The options.
	 * \return The App object representing the remote application or null if there is no such application.
	 */
	std::unique_ptr<App> connect(const std::string& name, int options = 0);

	/**
	 * Connects to an application with id.
	 * \param id The id.
	 * \param options The options.
	 * \return The App object representing the remote application or null if there is no such application.
	 */
	std::unique_ptr<App> connect(int id, int options = 0);

	/**
	 * Function provided by convenience to kill all the applications with name.
	 * \param name The name of the applications.
	 */
	void killAllAndWaitFor(const std::string& name);

	/**
	 * Gets the list of application configurations.
	 * \return The list of configurations.
	 */
	std::vector<App::Config> getApplicationConfigs() const;

	/**
	 * Gets the list of application infos.
	 * \return The list of infos.
	 */
	std::vector<App::Info> getApplicationInfos() const;

	/**
	 * Gets the list of application infos for the applications with name.
	 * \param name The name of the applications.
	 * \return The list of infos.
	 */
	std::vector<App::Info> getApplicationInfos(const std::string& name) const;

	/**
	 * Gets the list of ports owned by the Cameo applications.
	 * \return The list of ports.
	 */
	std::vector<App::Port> getPorts() const;

	/**
	 * Gets the actual state of an application.
	 * \param id The id of the application.
	 * \return The actual state.
	 */
	State getActualState(int id) const;

	/**
	 * Gets the past states of an application.
	 * \param id The id of the application.
	 * \return The set of states.
	 */
	std::set<State> getPastStates(int id) const;

	/**
	 * Creates an event stream socket.
	 * \return The new EventStreamSocket object.
	 */
	std::unique_ptr<EventStreamSocket> createEventStreamSocket();

	/**
	 * Creates a connection handler with polling time.
	 * \param handler The connection handler.
	 * \param pollingTimeMs The polling time in milliseconds.
	 * \return The new ConnectionChecker object.
	 */
	std::unique_ptr<ConnectionChecker> createConnectionChecker(ConnectionCheckerType handler, int pollingTimeMs = 10000);

	/**
	 * Class used for filtering events.
	 */
	class FilteredEventListener {

	public:
		/**
		 * Constructor.
		 * \param listener The event listener.
		 * \param filtered True if is filtered.
		 */
		FilteredEventListener(EventListener * listener, bool filtered) : m_listener(listener), m_filtered(filtered) {}

		/**
		 * Gets the listener.
		 * \return The EventListener object.
		 */
		EventListener * getListener() const;

		/**
		 * Returns true if is filtered.
		 * \return True if is filtered.
		 */
		bool isFiltered() const;

	private:
		EventListener * m_listener;
		bool m_filtered;
	};

	/**
	 * Gets the event listeners. Copies the list.
	 * \return The list of FilteredEventListener objects.
	 */
	std::vector<FilteredEventListener> getEventListeners();

	/**
	 * Registers an event listener.
	 * \param listener The EventListener object.
	 * \param filtered True if is filtered.
	 */
	void registerEventListener(EventListener * listener, bool filtered = true);

	/**
	 * Unregisters an event listener.
	 * \param listener The EventListener object.
	 */
	void unregisterEventListener(EventListener * listener);

	/**
	 * Returns a string representation of this application.
	 * \return The string representation.
	 */
	std::string toString() const override;

private:
	Server(const Endpoint& endpoint, bool useProxy);
	Server(const std::string& endpoint, bool useProxy);

	int getResponderProxyPort() const;
	int getPublisherProxyPort() const;
	int getSubscriberProxyPort() const;

	std::unique_ptr<App> makeInstance();
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

	std::string m_serverEndpointString;
	Endpoint m_serverEndpoint;
	int m_timeout;
	bool m_useProxy;
	std::array<int, 3> m_serverVersion;
	int m_responderProxyPort;
	int m_publisherProxyPort;
	int m_subscriberProxyPort;
	int m_serverStatusPort;
	int m_statusPort;
	std::shared_ptr<Context> m_context;
	std::unique_ptr<RequestSocket> m_requestSocket;

	std::mutex m_eventListenersMutex;
	std::vector<FilteredEventListener> m_eventListeners;
	std::unique_ptr<EventThread> m_eventThread;

	const static std::string CAMEO_SERVER;
};

/**
 * Stream operator for a Server object.
 */
std::ostream& operator<<(std::ostream&, const Server&);

}

#endif
