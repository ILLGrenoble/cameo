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

#include "InvalidArgumentException.h"
#include "UnregisteredApplicationException.h"
#include "SocketException.h"
#include "ConnectionTimeout.h"
#include "UndefinedApplicationException.h"
#include "UndefinedKeyException.h"
#include "KeyAlreadyExistsException.h"
#include "Response.h"
#include "Serializer.h"
#include "Context.h"
#include "TimeCondition.h"
#include "EventListener.h"
#include "JSON.h"
#include "KeyValue.h"
#include "Strings.h"
#include <functional>
#include <vector>
#include <set>
#include <memory>
#include <optional>

namespace cameo {

/**
 * Option output stream.
 */
const int OUTPUTSTREAM = 1;

class Server;
class EventStreamSocket;
class OutputStreamSocket;
class Waiting;
class WaitingSet;
class HandlerImpl;
class RequestSocket;

namespace application {

class Instance;

typedef int32_t State;

#undef ERROR

const State UNKNOWN          = 0;
const State STARTING         = 1;
const State RUNNING          = 2;
const State STOPPING         = 4;
const State KILLING          = 8;
const State PROCESSING_ERROR = 16;
const State FAILURE          = 32;
const State SUCCESS          = 64;
const State STOPPED          = 128;
const State KILLED           = 256;

/** \class This
 * \brief class managing the current CAMEO application.
 *
 * \details The application has to be launched by CAMEO command line or another CAMEO app
 * \todo why this does not inherit from the Instance class?
 */
class This : private EventListener {

	friend class cameo::Waiting;
	friend class cameo::Server;
	friend std::ostream& operator<<(std::ostream&, const cameo::application::This&);

public:
	typedef std::function<void()> StopFunctionType;

	/**
	 * Class defining the Communication Operations Manager (COM).
	 */
	class Com {

		friend class This;

	public:
		Context* getContext() const;

		void storeKeyValue(const std::string& key, const std::string& value) const;
		std::string getKeyValue(const std::string& key) const;
		void removeKey(const std::string& key) const;

		int requestPort() const;
		void setPortUnavailable(int port) const;
		void releasePort(int port) const;

		std::unique_ptr<RequestSocket> createRequestSocket(const std::string& endpoint) const;
		std::unique_ptr<RequestSocket> createRequestSocket(const std::string& endpoint, int timeout) const;

	private:
		Com(Server* server, int applicationId);

		Server* m_server;
		int m_applicationId;
	};

	This();
	~This();

	static void init(int argc, char* argv[]);
	static void init(const std::string& name, const std::string& endpoint);

	/**
	 * The terminate call is not necessary unless the static instance of This is not destroyed
	 * automatically.
	 */
	static void terminate();

	/// \brief returns the name of the CAMEO application corresponding to this instance
	static const std::string& getName(); 
	static int getId(); ///< returns the ID number of the instance
	static void setTimeout(int value);
	static int getTimeout();
	static const Endpoint& getEndpoint(); ///< returns the TCP address of this instance
	static Server& getServer();
	static const Com& getCom();

	/**
	 * throws StarterServerException.
	 */
	static Server& getStarterServer();
	static bool isAvailable(int timeout = 10000);
	static bool isStopping();

	/**
	 * Sets the stop handler with stopping time that overrides the one that may be defined in the
	 * configuration of the server.
	 */
	static void handleStop(StopFunctionType function, int stoppingTime = -1);

	static void cancelWaitings();

	static bool setRunning(); ///< sets the current instance in RUNNING state

	/**
	 * Sets the result.
	 */
	static void setBinaryResult(const std::string& data);
	static void setResult(const std::string& data);

	/**
	 * Connects to the starter application, i.e. the application which started this application.
	 */
	static std::unique_ptr<Instance> connectToStarter();

private:
	void initApplication(int argc, char* argv[]);
	void initApplication(const std::string& name, const std::string& endpoint);
	void initApplication();

	static State parseState(const std::string& value);
	State getState(int id) const;

	int initUnregisteredApplication();
	void terminateUnregisteredApplication();

	State waitForStop();

	void stoppingFunction(StopFunctionType stop);
	void handleStopImpl(StopFunctionType function, int stoppingTime);

	std::string m_name;
	int m_id;
	bool m_registered;

	Endpoint m_serverEndpoint;
	Endpoint m_starterEndpoint;
	std::string m_starterName;
	int m_starterId;

	std::unique_ptr<Server> m_server;
	std::unique_ptr<Server> m_starterServer;
	std::unique_ptr<Com> m_com;

	std::unique_ptr<WaitingSet> m_waitingSet;
	std::unique_ptr<HandlerImpl> m_stopHandler;

	bool m_inited;

	static This m_instance;
	static const std::string RUNNING_STATE;
};


class Instance : private EventListener {

	friend class cameo::Server;
	friend std::ostream& operator<<(std::ostream&, const Instance&);

public:
	class Com {

		friend class Instance;

	public:
		std::string getKeyValue(const std::string& key) const;

	private:
		Com(Server* server);

		Server* m_server;
		int m_applicationId;
	};

	~Instance();

	const std::string& getName() const;
	int getId() const;
	Endpoint getEndpoint() const;
	Endpoint getStatusEndpoint() const;
	std::string getNameId() const;
	const Com& getCom() const;

	bool hasResult() const;
	bool exists() const;
	const std::string& getErrorMessage() const;
	bool stop();
	bool kill();

	State waitFor(int states);
	State waitFor();
	State waitFor(KeyValue& keyValue);

	void cancelWaitFor(); // to unblock another instance

	/**
	 * Deprecated.
	 * TODO remove in next version.
	 */
	State now();

	/**
	 * Gets the last state.
	 */
	State getLastState();

	/**
	 * Returns the actual state and UNKNOWN if the instance does not exist anymore.
	 */
	State getActualState() const;

	/**
	 * Returns the past states.
	 */
	std::set<State> getPastStates() const;

	/**
	 * Returns the exit code.
	 */
	int getExitCode() const;

	std::optional<std::string> getBinaryResult();
	std::optional<std::string> getResult();

	std::unique_ptr<OutputStreamSocket> getOutputStreamSocket();

private:
	Instance(Server* server);

	void setId(int id);
	void setErrorMessage(const std::string& message);
	void setOutputStreamSocket(std::unique_ptr<OutputStreamSocket>& socket);
	void setPastStates(State pastStates);
	void setInitialState(State state);
	State waitFor(int states, KeyValue& keyValue, bool blocking);

	Server* m_server;
	std::unique_ptr<OutputStreamSocket> m_outputStreamSocket;
	int m_id;
	std::string m_errorMessage;
	Com m_com;

	int m_pastStates;
	State m_initialState;
	State m_lastState;
	bool m_hasResult;
	std::string m_resultData;
	int m_exitCode;
};

///////////////////////////////////////////////////////////////////////////
// InstanceArray

typedef std::vector<std::unique_ptr<Instance>> InstanceArray;


///////////////////////////////////////////////////////////////////////////
// Configuration

class Configuration {

	friend std::ostream& operator<<(std::ostream&, const Configuration&);

public:
	Configuration(const std::string& name, const std::string& description, bool singleInfo, bool restart,
		      int startingTime, int stoppingTime);

	const std::string& getName() const;
	const std::string& getDescription() const;
	bool hasSingleInstance() const;
	bool canRestart() const;
	int getStartingTime() const;
	int getStoppingTime() const;

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

class Info {

	friend std::ostream& operator<<(std::ostream&, const Info&);

public:
	Info(const std::string& name, int id, int pid, State applicationState, State pastApplicationStates,
	     const std::string& args);

	int getId() const;
	State getState() const;
	State getPastStates() const;
	const std::string& getArgs() const;
	const std::string& getName() const;
	int getPid() const;

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

class Port {

	friend std::ostream& operator<<(std::ostream&, const Port&);

public:
	Port(int port, const std::string& status, const std::string& owner);

	int getPort() const;
	const std::string& getStatus() const;
	const std::string& getOwner() const;

private:
	int m_port;
	std::string m_status;
	std::string m_owner;
};

std::string toString(cameo::application::State applicationStates);
std::ostream& operator<<(std::ostream&, const cameo::application::This&);
std::ostream& operator<<(std::ostream&, const cameo::application::Instance&);
std::ostream& operator<<(std::ostream&, const cameo::application::Configuration&);
std::ostream& operator<<(std::ostream&, const cameo::application::Info&);
std::ostream& operator<<(std::ostream&, const cameo::application::Port&);

} // namespace application
} // namespace cameo

#endif
