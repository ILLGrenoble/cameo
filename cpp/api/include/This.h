/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

#ifndef CAMEO_THIS_H_
#define CAMEO_THIS_H_

#include "ServerAndApp.h"

namespace cameo {

/**
 * Class managing the current Cameo application.
 *
 * The application can be launched by the Cameo console or another Cameo App.
 */
class CAMEO_EXPORT This : private EventListener {

	friend class cameo::Waiting;
	friend class cameo::Server;
	friend std::ostream& operator<<(std::ostream&, const cameo::This&);

public:
	typedef std::function<void()> StopFunctionType;

	/**
	 * Class defining the Communication Operations Manager (COM) for this application.
	 *
	 * It facilitates the definition of communication objects.
	 */
	class CAMEO_EXPORT Com {

		friend class This;

	public:
		/**
		 * Gets the communication context. Shall be a ZeroMQ context i.e. a ContextZmq instance.
		 * \return The context.
		 */
		Context* getContext() const;

		/**
		 * Gets the responder proxy port.
		 * \return The port.
		 */
		int getResponderProxyPort() const;

		/**
		 * Gets the publisher proxy port.
		 * \return The port.
		 */
		int getPublisherProxyPort() const;

		/**
		 * Gets the subscriber proxy port.
		 * \return The port.
		 */
		int getSubscriberProxyPort() const;

		/**
		 * Stores the key value in the Cameo server.
		 * \param key The key.
		 * \param value The value.
		 */
		void storeKeyValue(const std::string &key, const std::string &value) const;

		/**
		 * Gets the key value from the Cameo server.
		 * \param key The key.
		 * \return The value associated to key.
		 */
		std::string getKeyValue(const std::string &key) const;

		/**
		 * Removes the key from the Cameo server.
		 * \param key The key.
		 */
		void removeKey(const std::string &key) const;

		/**
		 * Requests a new port from the Cameo server.
		 * \return An available port.
		 */
		int requestPort() const;

		/**
		 * Tells the Cameo server that the port is not availaible i.e. another application onws it.
		 * \param port The port.
		 */
		void setPortUnavailable(int port) const;

		/**
		 * Releases the port so that the Cameo server will be able to return it in a future request.
		 * \param port The port.
		 */
		void releasePort(int port) const;

		/**
		 * Creates a request socket.
		 * \return A new request socket.
		 */
		std::unique_ptr<RequestSocket> createRequestSocket(const std::string &endpoint, const std::string &responderIdentity) const;

		/**
		 * Creates a request socket with a timeout.
		 * \return A new request socket.
		 */
		std::unique_ptr<RequestSocket> createRequestSocket(const std::string &endpoint, const std::string &responderIdentity, int timeout) const;

	private:
		Com(Server *server, int applicationId);

		Server *m_server;
		int m_applicationId;
	};

	/**
	 * Destructor.
	 */
	~This();

	/**
	 * Initializes this application from the main arguments.
	 * \param argc The number of arguments.
	 * \param argv The arguments.
	 */
	static void init(int argc, char *argv[]);

	/**
	 * Initializes this application with explicit parameters.
	 * \param name The Cameo name.
	 * \param endpoint The Cameo server endpoint e.g. tcp://myhost:7000.
	 */
	static void init(const std::string &name, const Endpoint &endpoint);

	/**
	 * Initializes this application with explicit parameters.
	 * \param name The Cameo name.
	 * \param endpoint The Cameo server endpoint e.g. tcp://myhost:7000.
	 */
	static void init(const std::string &name, const std::string &endpoint);

	/**
	 * Terminates the application.
	 */
	static void terminate();

	/**
	 * Returns the Cameo name of this application.
	 * \return The Cameo name.
	 */
	static const std::string& getName();

	/**
	 * Returns the Cameo id of this application.
	 * \return The Cameo id.
	 */
	static int getId();

	/**
	 * Sets the timeout.
	 * \param value The timeout value.
	 */
	static void setTimeout(int value);

	/**
	 * Gets the timeout.
	 * \return The timeout value.
	 */
	static int getTimeout();

	/**
	 * Returns the endpoint of the Cameo server.
	 * \return The Cameo endpoint.
	 */
	static const Endpoint& getEndpoint();

	/**
	 * Returns the Cameo server that owns this application.
	 * \return The Server instance.
	 */
	static Server& getServer();

	/**
	 * Returns the COM object.
	 * \return The Com object.
	 */
	static const Com& getCom();

	/**
	 * Returns true if the Cameo server that owns this application is available.
	 * \param timeout The timeout value.
	 * \return True if the Cameo replies within the timeout.
	 */
	static bool isAvailable(int timeout = 10000);

	/**
	 * Returns true if the application is in STOPPING state.
	 * \return True or false.
	 */
	static bool isStopping();

	/**
	 * Sets the stop handler with stopping time that overrides the one that may be defined in the
	 * configuration of the server.
	 * \param function The stop handler.
	 * \param stoppingTime The stopping time in milliseconds.
	 */
	static void handleStop(StopFunctionType function, int stoppingTime = -1);

	/**
	 * Cancels all the waiting calls.
	 */
	static void cancelAll();

	/**
	 * Sets this application in RUNNING state.
	 * \return True or false.
	 */
	static bool setRunning();

	/**
	 * Sets the result.
	 * \param data The string result.
	 */
	static void setResult(const std::string &data);

	/**
	 * Connects to the starter application, i.e. the application which started this application.
	 * The server and instance are returned. Be careful, the instance is linked to the server, so it must not be destroyed before.
	 * \param options The options passed to connect the starter app.
	 * \param timeout Timeout for the server initialization.
	 */
	static std::unique_ptr<ServerAndApp> connectToStarter(int options = 0, int timeout = 0);

	/**
	 * Returns a string representation of this application.
	 * \return The string representation.
	 */
	static std::string toString();

private:
	This();

	void terminateImpl();

	void initApplication(int argc, char *argv[]);
	void initApplication(const std::string &name, const Endpoint &endpoint);
	void initApplication(const std::string &name, const std::string &endpoint);
	void initApplication();

	static state::Value parseState(const std::string &value);
	state::Value getState(int id) const;

	int initUnregisteredApplication();
	void terminateUnregisteredApplication();

	void initStopCheck(StopFunctionType function, int stoppingTime);
	void stop();
	void checkStates();
	void initStarterCheck();
	void startCheckStatesThread();

	std::string m_name;
	int m_id;
	bool m_registered;

	Endpoint m_serverEndpoint;
	Endpoint m_starterEndpoint;
	std::string m_starterName;
	int m_starterId;
	int m_starterProxyPort;
	bool m_starterLinked;

	std::unique_ptr<Server> m_server;
	std::unique_ptr<Com> m_com;

	std::unique_ptr<WaitingSet> m_waitingSet;

	StopFunctionType m_stopFunction;
	std::unique_ptr<Server> m_starterServer;
	std::unique_ptr<std::thread> m_checkStatesThread;

	bool m_inited;

	static This m_instance;
	static const std::string RUNNING_STATE;
};


}

#endif