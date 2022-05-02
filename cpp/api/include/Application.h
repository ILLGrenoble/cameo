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

#ifndef CAMEO_APPLICATION_H_
#define CAMEO_APPLICATION_H_

#include "Object.h"
#include "Cancelable.h"
#include "Timeoutable.h"
#include "InvalidArgumentException.h"
#include "UnregisteredApplicationException.h"
#include "SocketException.h"
#include "ConnectionTimeout.h"
#include "UndefinedApplicationException.h"
#include "UndefinedKeyException.h"
#include "KeyAlreadyExistsException.h"
#include "AppException.h"
#include "InitException.h"
#include "Response.h"
#include "Context.h"
#include "TimeCondition.h"
#include "EventListener.h"
#include "JSON.h"
#include "KeyValue.h"
#include "Strings.h"
#include "IdGenerator.h"
#include "RequestSocket.h"
#include "Waiting.h"
#include "Messages.h"
#include <functional>
#include <vector>
#include <set>
#include <memory>
#include <optional>

/**
 * Main namespace of the library.
 */
namespace cameo {

/**
 * Option output stream.
 */
const int OUTPUTSTREAM = 1;

/**
 * Option unlinked.
 */
const int UNLINKED = 2;

class Server;
class EventStreamSocket;
class OutputStreamSocket;
class Waiting;
class WaitingSet;
class StopHandler;
class RequestSocket;

class App;

/**
 * Helper class to provide an App instance and its associated Server instance.
 */
class ServerAndApp {

public:
	/**
	 * Constructor.
	 * \param server The server.
	 * \param app The app started by the server.
	 */
	ServerAndApp(std::unique_ptr<Server>& server, std::unique_ptr<App>& app);

	/**
	 * Gets the server.
	 * \return The server.
	 */
	Server& getServer();

	/**
	 * Gets the app.
	 * \return The app.
	 */
	App& getApp();

	/**
	 * Terminates the server and app.
	 */
	void terminate();

private:
	std::unique_ptr<Server> m_server;
	std::unique_ptr<App> m_app;
};

/**
 * Alias for the state of an App.
 */
typedef int32_t State;

#undef ERROR

/**
 * Unknown state.
 */
const State NIL          = 0;

/**
 * Starting state.
 */
const State STARTING         = 1;

/**
 * Running state.
 */
const State RUNNING          = 2;

/**
 * Stopping state.
 */
const State STOPPING         = 4;

/**
 * Killing state.
 */
const State KILLING          = 8;

/**
 * Processing error state.
 */
const State PROCESSING_ERROR = 16;

/**
 * Failure state.
 */
const State FAILURE          = 32;

/**
 * Success state.
 */
const State SUCCESS          = 64;

/**
 * Stopped state.
 */
const State STOPPED          = 128;

/**
 * Killed state.
 */
const State KILLED           = 256;

///////////////////////////////////////////////////////////////////////////
// This

/**
 * Class managing the current Cameo application.
 *
 * The application can be launched by the Cameo console or another Cameo App.
 */
class This : private EventListener {

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
	class Com {

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
		void storeKeyValue(const std::string& key, const std::string& value) const;

		/**
		 * Gets the key value from the Cameo server.
		 * \param key The key.
		 * \return The value associated to key.
		 */
		std::string getKeyValue(const std::string& key) const;

		/**
		 * Removes the key from the Cameo server.
		 * \param key The key.
		 */
		void removeKey(const std::string& key) const;

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
		std::unique_ptr<RequestSocket> createRequestSocket(const std::string& endpoint, const std::string& responderIdentity) const;

		/**
		 * Creates a request socket with a timeout.
		 * \return A new request socket.
		 */
		std::unique_ptr<RequestSocket> createRequestSocket(const std::string& endpoint, const std::string& responderIdentity, int timeout) const;

	private:
		Com(Server* server, int applicationId);

		Server* m_server;
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
	static void init(int argc, char* argv[]);

	/**
	 * Initializes this application with direct parameters.
	 * \param name The Cameo name.
	 * \param endpoint The Cameo server endpoint e.g. tcp://myhost:7000.
	 */
	static void init(const std::string& name, const std::string& endpoint);

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
	static void setResult(const std::string& data);

	/**
	 * Connects to the starter application, i.e. the application which started this application.
	 * The server and instance are returned. Be careful, the instance is linked to the server, so it must not be destroyed before.
	 * \param options The options passed to connect the starter app.
	 * \param useProxy True if the proxy is used to connect to the starter app.
	 */
	static std::unique_ptr<ServerAndApp> connectToStarter(int options = 0, bool useProxy = false);

	/**
	 * Returns a string representation of this application.
	 * \return The string representation.
	 */
	static std::string toString();

private:
	This();

	void terminateImpl();

	void initApplication(int argc, char* argv[]);
	void initApplication(const std::string& name, const std::string& endpoint);
	void initApplication();

	static State parseState(const std::string& value);
	State getState(int id) const;

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

///////////////////////////////////////////////////////////////////////////
// App

/**
 * Class defining a remote Cameo application.
 *
 * An App instance is created by a Server instance. It represents a real remote application that was started by a real Cameo server.
 */
class App : private EventListener {

	friend class cameo::Server;
	friend std::ostream& operator<<(std::ostream&, const App&);

public:

	/**
	 * Class defining the Communication Operations Manager (COM) for an App instance.
	 *
	 * It facilitates the definition of communication objects.
	 */
	class Com {

		friend class App;

	public:
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
		 * Gets the key value.
		 * \param key The key.
		 * \return The value associated to key.
		 */
		std::string getKeyValue(const std::string& key) const;

		/**
		 * Class defining an exception when getting a key value fails.
		 */
		class KeyValueGetterException : public RemoteException {

		public:
			/**
			 * Constructor.
			 * \param message The message.
			 */
			KeyValueGetterException(const std::string& message);
		};

		/**
		 * Class defining a getter for a key value.
		 */
		class KeyValueGetter : private EventListener {

			friend class Com;

		public:
			/**
			 * Destructor.
			 */
			virtual ~KeyValueGetter();

			/**
			 * Gets the value.
			 * \return The value.
			 */
			std::string get();

			/**
			 * Cancels the get call.
			 */
			void cancel();

		private:
			KeyValueGetter(Server* server, const std::string& name, int id, const std::string& key);

			Server* m_server;
			int m_id;
			std::string m_key;
		};

		/**
		 * Creates a KeyValueGetter for a key.
		 * \return A new KeyValueGetter instance.
		 */
		std::unique_ptr<KeyValueGetter> getKeyValueGetter(const std::string& key) const;

	private:
		Com(Server* server);

		Server* m_server;
		int m_applicationId;
		std::string m_name;
	};

	///////////////////////////////////////////////////////////////////////////
	// Config

	/**
	 * Class defining the configuration of a registered application.
	 */
	class Config {

		friend std::ostream& operator<<(std::ostream&, const Config&);

	public:
		/**
		 * Constructor.
		 * \param name The name.
		 * \param description The description.
		 * \param singleInstance True if there is only a single instance.
		 * \param restart True if the application can restart.
		 * \param startingTime Starting time in seconds.
		 * \param stoppingTime Stopping time in seconds.
		 */
		Config(const std::string& name, const std::string& description, bool singleInstance, bool restart,
			      int startingTime, int stoppingTime);

		/**
		 * Gets the name.
		 * \return The name.
		 */
		const std::string& getName() const;

		/**
		 * Gets the description.
		 * \return The description.
		 */
		const std::string& getDescription() const;

		/**
		 * Returns the multiplicity of the application.
		 * \return True if the application runs only once.
		 */
		bool hasSingleInstance() const;

		/**
		 * Returns true if the application can restart.
		 * \return True if the application can restart.
		 */
		bool canRestart() const;

		/**
		 * Returns the starting time.
		 * \return The starting time in seconds.
		 */
		int getStartingTime() const;

		/**
		 * Returns the stopping time.
		 * \return The stopping time in seconds.
		 */
		int getStoppingTime() const;

		/**
		 * Returns a string representation of this application.
		 * \return The string representation.
		 */
		std::string toString() const;

	private:
		std::string m_name;
		std::string m_description;
		bool m_singleInstance;
		bool m_restart;
		int m_startingTime;
		int m_stoppingTime;
	};

	///////////////////////////////////////////////////////////////////////////
	// Info

	/**
	 * Class showing the information of a running Cameo application.
	 */
	class Info {

		friend std::ostream& operator<<(std::ostream&, const Info&);

	public:
		/**
		 * Constructor.
		 * \param name The name.
		 * \param id The Cameo id.
		 * \param pid The PID.
		 * \param applicationState The current application state.
		 * \param pastApplicationStates The past application states.
		 * \param args The arguments of the executable.
		 */
		Info(const std::string& name, int id, int pid, State applicationState, State pastApplicationStates,
		     const std::string& args);

		/**
		 * Gets the id.
		 * \return The id.
		 */
		int getId() const;

		/**
		 * Gets the state.
		 * \return The state.
		 */
		State getState() const;

		/**
		 * Gets the past states.
		 * \return the past states.
		 */
		State getPastStates() const;

		/**
		 * Gets the arguments of the executable.
		 * \return The arguments.
		 */
		const std::string& getArgs() const;

		/**
		 * Gets the name.
		 * \return The name.
		 */
		const std::string& getName() const;

		/**
		 * Gets the PID of the process.
		 * \return The PID.
		 */
		int getPid() const;

		/**
		 * Returns a string representation of this application.
		 * \return The string representation.
		 */
		std::string toString() const;

	private:
		int m_id;
		int m_pid;
		State m_applicationState;
		State m_pastApplicationStates;
		std::string m_processState;
		std::string m_args;
		std::string m_name;
	};

	///////////////////////////////////////////////////////////////////////////
	// Port

	/**
	 * Class defining a system port associated to a Cameo application.
	 */
	class Port {

		friend std::ostream& operator<<(std::ostream&, const Port&);

	public:
		/**
		 * Constructor.
		 * \param port The port.
		 * \param status The status.
		 * \param owner The owner.
		 */
		Port(int port, const std::string& status, const std::string& owner);

		/**
		 * Gets the port.
		 * \return The port.
		 */
		int getPort() const;

		/**
		 * Gets the status.
		 * \return The status.
		 */
		const std::string& getStatus() const;

		/**
		 * Gets the owner.
		 * \return The owner.
		 */
		const std::string& getOwner() const;

		/**
		 * Returns a string representation of this application.
		 * \return The string representation.
		 */
		std::string toString() const;

	private:
		int m_port;
		std::string m_status;
		std::string m_owner;
	};

	/**
	 * Destructor.
	 */
	~App();

	/**
	 * Terminates the communication. The object is not usable after this call.
	 */
	void terminate();

	/**
	 * Gets the name.
	 * \return The name.
	 */
	const std::string& getName() const;

	/**
	 * Gets the id.
	 * \return the Cameo id.
	 */
	int getId() const;

	/**
	 * Returns the use of the proxy.
	 * \return True if the proxy is used.
	 */
	bool usesProxy() const;

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
	 * Gets the string concatenation of the name and the id.
	 * \return the name id.
	 */
	std::string getNameId() const;

	/**
	 * Gets the COM object providing a helper to write the communication classes.
	 * \return The Com object.
	 */
	const Com& getCom() const;

	/**
	 * Returns true if a result was received from the remote application.
	 * \return True if there is a result.
	 */
	bool hasResult() const;

	/**
	 * Stops the remote application.
	 * The call is not blocking, so it must be followed by a call to waitFor to ensure the termination of the application.
	 * \return True if the request succeeded.
	 */
	bool stop();

	/**
	 * Kills the remote application.
	 * \return True if the request succeeded.
	 */
	bool kill();

	/**
	 * Waits for the states.
	 * The method is not thread-safe and must not be called concurrently.
	 * \return The state when the call returned.
	 */
	State waitFor(int states);

	/**
	 * Waits for the termination of the application.
	 * The method is not thread-safe and must not be called concurrently.
	 * \return The terminal state.
	 */
	State waitFor();

	/**
	 * Waits for the key value.
	 * The method is not thread-safe and must not be called concurrently.
	 * \param keyValue The key value.
	 * \return The state when the call returned.
	 */
	State waitFor(KeyValue& keyValue);

	/**
	 * Cancels the blocking waitFor() in another thread.
	 */
	void cancel();

	/**
	 * Gets the last state.
	 * \return The last state.
	 */
	State getLastState();

	/**
	 * Returns the actual state and NIL if the instance does not exist anymore.
	 * \return The actual state.
	 */
	State getActualState() const;

	/**
	 * Returns the past states.
	 * \return The past states.
	 */
	std::set<State> getPastStates() const;

	/**
	 * Returns the exit code.
	 * \return The exit code.
	 */
	int getExitCode() const;

	/**
	 * Gets the initial state.
	 * \return The initial state.
	 */
	State getInitialState() const;

	/**
	 * Returns the result if there is one.
	 * \return The result that may not exist.
	 */
	std::optional<std::string> getResult();

	/**
	 * Gets the output stream socket.
	 * \return The output stream socket.
	 */
	std::unique_ptr<OutputStreamSocket> getOutputStreamSocket();

	/**
	 * Returns a string representation of this application.
	 * \return The string representation.
	 */
	std::string toString() const;

private:
	App(Server* server);

	void setId(int id);
	void setOutputStreamSocket(std::unique_ptr<OutputStreamSocket>& socket);
	void setPastStates(State pastStates);
	void setInitialState(State state);
	State waitFor(int states, KeyValue& keyValue, bool blocking);

	Server* m_server;
	std::unique_ptr<OutputStreamSocket> m_outputStreamSocket;
	int m_id;
	Com m_com;

	int m_pastStates;
	State m_initialState;
	State m_lastState;
	bool m_hasResult;
	std::string m_resultData;
	int m_exitCode;
};

///////////////////////////////////////////////////////////////////////////
// AppArray

/**
 * Array of App objects.
 */
typedef std::vector<std::unique_ptr<App>> AppArray;


/**
 * Converts a set of states to a string.
 */
std::string toString(cameo::State applicationStates);

/**
 * Stream operator for an App object.
 */
std::ostream& operator<<(std::ostream&, const cameo::App&);

/**
 * Stream operator for a Config object.
 */
std::ostream& operator<<(std::ostream&, const cameo::App::Config&);

/**
 * Stream operator for an Info object.
 */
std::ostream& operator<<(std::ostream&, const cameo::App::Info&);

/**
 * Stream operator for a Port object.
 */
std::ostream& operator<<(std::ostream&, const cameo::App::Port&);

}

#endif
