/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
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
#include "SynchronizationTimeout.h"
#include "UndefinedApplicationException.h"
#include "UndefinedKeyException.h"
#include "KeyAlreadyExistsException.h"
#include "AppException.h"
#include "InitException.h"
#include "Response.h"
#include "Context.h"
#include "TimeCondition.h"
#include "EventListener.h"
#include "KeyValue.h"
#include "Strings.h"
#include "IdGenerator.h"
#include "Waiting.h"
#include "Messages.h"
#include <functional>
#include <vector>
#include <set>
#include <memory>
#include <optional>
#include "TimeoutCounter.h"

/**
 * Main namespace of the library.
 */
namespace cameo {
namespace option {

/**
 * Option output stream.
 */
const int NONE = 0;

/**
 * Option output stream.
 */
const int OUTPUTSTREAM = 1;

/**
 * Option unlinked.
 */
const int UNLINKED = 1 << 1;

/**
 * Option use proxy.
 */
const int USE_PROXY = 1 << 2;

}

class Server;
class EventStreamSocket;
class OutputStreamSocket;
class Waiting;
class WaitingSet;
class StopHandler;
class RequestSocket;

namespace state {

typedef int32_t Value;

/**
 * Unknown state.
 */
const Value NIL = 0;

/**
 * Starting state.
 */
const Value STARTING = 1;

/**
 * Running state.
 */
const Value RUNNING = 2;

/**
 * Stopping state.
 */
const Value STOPPING = 4;

/**
 * Killing state.
 */
const Value KILLING = 8;

/**
 * Processing error state.
 */
const Value PROCESSING_FAILURE = 16;

/**
 * Failure state.
 */
const Value FAILURE = 32;

/**
 * Success state.
 */
const Value SUCCESS = 64;

/**
 * Stopped state.
 */
const Value STOPPED = 128;

/**
 * Killed state.
 */
const Value KILLED = 256;

}


///////////////////////////////////////////////////////////////////////////
// App

/**
 * Class defining a remote Cameo application.
 *
 * An App instance is created by a Server instance. It represents a real remote application that was started by a real Cameo server.
 */
class CAMEO_EXPORT App : private EventListener {

	friend class cameo::Server;

public:

	/**
	 * Class defining the Communication Operations Manager (COM) for an App instance.
	 *
	 * It facilitates the definition of communication objects.
	 */
	class CAMEO_EXPORT Com {

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
		std::string getKeyValue(const std::string &key) const;

		/**
		 * Class defining an exception when getting a key value fails.
		 */
		class KeyValueGetterException: public RemoteException {

		public:
			/**
			 * Constructor.
			 * \param message The message.
			 */
			KeyValueGetterException(const std::string &message);
		};

		/**
		 * Class defining a getter for a key value.
		 */
		class CAMEO_EXPORT KeyValueGetter: private EventListener {

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
			std::string get(const TimeoutCounter& timeout);

			/**
			 * Cancels the get call.
			 */
			void cancel();

			/**
			 * Returns true if is canceled.
			 * \return True if is canceled.
			 */
			bool isCanceled() const;

		private:
			KeyValueGetter(Server *server, const std::string &name, int id, const std::string &key);

			Server *m_server;
			int m_id;
			std::string m_key;
			std::atomic_bool m_canceled;
		};

		/**
		 * Creates a KeyValueGetter for a key.
		 * \return A new KeyValueGetter instance.
		 */
		std::unique_ptr<KeyValueGetter> createKeyValueGetter(const std::string &key) const;

	private:
		Com(Server *server);

		Server *m_server;
		int m_applicationId;
		std::string m_name;
	};

	///////////////////////////////////////////////////////////////////////////
	// Config

	/**
	 * Class defining the configuration of a registered application.
	 */
	class CAMEO_EXPORT Config {

	public:
		/**
		 * Constructor.
		 * \param name The name.
		 * \param description The description.
		 * \param multiple The maximum number of running apps.
		 * \param restart True if the application can restart.
		 * \param startingTime Starting time in seconds.
		 * \param stoppingTime Stopping time in seconds.
		 */
		Config(const std::string &name, const std::string &description, int multiple, bool restart, int startingTime, int stoppingTime);

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
		 * \return The maximum number of running apps.
		 */
		int getMultiple() const;

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
		int m_multiple;
		bool m_restart;
		int m_startingTime;
		int m_stoppingTime;
	};

	///////////////////////////////////////////////////////////////////////////
	// Info

	/**
	 * Class showing the information of a running Cameo application.
	 */
	class CAMEO_EXPORT Info {

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
		Info(const std::string &name, int id, int pid, state::Value applicationState, state::Value pastApplicationStates, const std::string &args);

		/**
		 * Gets the id.
		 * \return The id.
		 */
		int getId() const;

		/**
		 * Gets the state.
		 * \return The state.
		 */
		state::Value getState() const;

		/**
		 * Gets the past states.
		 * \return the past states.
		 */
		state::Value getPastStates() const;

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
		state::Value m_applicationState;
		state::Value m_pastApplicationStates;
		std::string m_processState;
		std::string m_args;
		std::string m_name;
	};

	///////////////////////////////////////////////////////////////////////////
	// Port

	/**
	 * Class defining a system port associated to a Cameo application.
	 */
	class CAMEO_EXPORT Port {

	public:
		/**
		 * Constructor.
		 * \param port The port.
		 * \param status The status.
		 * \param owner The owner.
		 */
		Port(int port, const std::string &status, const std::string &owner);

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
	 * Connects to the same App. Is equivalent to cloning the App instance.
	 * \param options The options.
	 */
	std::unique_ptr<App> connect(int options = 0) const;

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
	state::Value waitFor(int states);

	/**
	 * Waits for the termination of the application.
	 * The method is not thread-safe and must not be called concurrently.
	 * \return The terminal state.
	 */
	state::Value waitFor();

	/**
	 * Waits for the key value.
	 * The method is not thread-safe and must not be called concurrently.
	 * \param keyValue The key value.
	 * \return The state when the call returned.
	 */
	state::Value waitFor(KeyValue &keyValue);

	/**
	 * Cancels the blocking waitFor() in another thread.
	 */
	void cancel();

	/**
	 * Gets the last state.
	 * \return The last state.
	 */
	state::Value getLastState();

	[[deprecated("Use getState() instead.")]]
	state::Value getActualState() const;

	/**
	 * Returns the current state and NIL if the instance does not exist anymore.
	 * \return The current state.
	 */
	state::Value getState() const;

	/**
	 * Returns the past states.
	 * \return The past states.
	 */
	std::set<state::Value> getPastStates() const;

	/**
	 * Returns the exit code.
	 * \return The exit code.
	 */
	int getExitCode() const;

	/**
	 * Gets the initial state.
	 * \return The initial state.
	 */
	state::Value getInitialState() const;

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
	App(Server *server);

	void setId(int id);
	void setOutputStreamSocket(std::unique_ptr<OutputStreamSocket> &socket);
	void setPastStates(state::Value pastStates);
	void setInitialState(state::Value state);
	state::Value waitFor(int states, KeyValue &keyValue, bool blocking);

	Server *m_server;
	std::unique_ptr<OutputStreamSocket> m_outputStreamSocket;
	int m_id;
	Com m_com;

	int m_pastStates;
	state::Value m_initialState;
	state::Value m_lastState;
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
CAMEO_EXPORT std::string toString(cameo::state::Value applicationStates);

}

/**
 * Stream operator for an App object.
 */
CAMEO_EXPORT std::ostream& operator<<(std::ostream&, const cameo::App&);

/**
 * Stream operator for a Config object.
 */
CAMEO_EXPORT std::ostream& operator<<(std::ostream&, const cameo::App::Config&);

/**
 * Stream operator for an Info object.
 */
CAMEO_EXPORT std::ostream& operator<<(std::ostream&, const cameo::App::Info&);

/**
 * Stream operator for a Port object.
 */
CAMEO_EXPORT std::ostream& operator<<(std::ostream&, const cameo::App::Port&);


#endif
