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

#include <functional>
#include <vector>
#include <set>
#include <memory>
#include "InvalidArgumentException.h"
#include "UnmanagedApplicationException.h"
#include "SocketException.h"
#include "ConnectionTimeout.h"
#include "PublisherCreationException.h"
#include "RequesterCreationException.h"
#include "ResponderCreationException.h"
#include "UndefinedApplicationException.h"
#include "UndefinedKeyException.h"
#include "Response.h"
#include "Serializer.h"
#include "Services.h"
#include "TimeCondition.h"
#include "EventListener.h"
#include "KeyValue.h"

namespace cameo {

enum Option {
	NONE = 0,
	OUTPUTSTREAM = 1
};

class Server;
class EventStreamSocket;
class OutputStreamSocket;
class PublisherImpl;
class SubscriberImpl;
class RequestImpl;
class ResponderImpl;
class RequesterImpl;
class WaitingImpl;
class SocketWaitingImpl;
class GenericWaitingImpl;
class WaitingImplSet;
class HandlerImpl;

namespace application {

// forward declarations
class Publisher;
class Subscriber;
class Responder;
class Requester;
class Instance;
class Waiting;

typedef int32_t State;

#undef ERROR

const State UNKNOWN = 0;
const State STARTING = 1;
const State RUNNING = 2;
const State STOPPING = 4;
const State KILLING = 8;
const State PROCESSING_ERROR = 16;
const State FAILURE = 32;
const State SUCCESS = 64;
const State STOPPED = 128;
const State KILLED = 256;


class This : private Services, private EventListener {

	friend class cameo::application::Publisher;
	friend class cameo::application::Responder;
	friend class cameo::application::Requester;
	friend class cameo::PublisherImpl;
	friend class cameo::RequestImpl;
	friend class cameo::ResponderImpl;
	friend class cameo::RequesterImpl;
	friend class cameo::SocketWaitingImpl;
	friend class cameo::GenericWaitingImpl;
	friend class cameo::Server;
	friend std::ostream& operator<<(std::ostream&, const cameo::application::This&);

	typedef std::function<void ()> StopFunctionType;

public:
	This();
	~This();

	static void init(int argc, char *argv[]);

	/**
	 * The terminate call is not necessary unless the static instance of This is not destroyed automatically.
	 */
	static void terminate();

	static const std::string& getName();
	static int getId();
	static void setTimeout(int timeout);
	static int getTimeout();
	static const std::string& getEndpoint();
	static Server& getServer();

	/**
	 * throws StarterServerException.
	 */
	static Server& getStarterServer();
	static const std::string& getUrl();
	static bool isAvailable(int timeout = 10000);
	static bool isStopping();

	template<typename Type>
	static void handleStop(Type function) {
		m_instance.handleStopImpl(function);
	}

	static void cancelWaitings();

	static bool setRunning();

	/**
	 * Sets the result.
	 */
	static void setBinaryResult(const std::string& data);
	static void setResult(const std::string& data);

	/**
	 * Connects to the starter application, i.e. the application which started this application.
	 */
	static std::unique_ptr<Instance> connectToStarter();

	static void storeKeyValue(const std::string& key, const std::string& value);
	static std::string getKeyValue(const std::string& key);
	static void removeKey(const std::string& key);

private:
	void initApplication(int argc, char *argv[]);

	static std::string getReference();
	static State parseState(const std::string& value);
	State getState(int id) const;

	int initUnmanagedApplication();
	void terminateUnmanagedApplication();

	bool destroyPublisher(const std::string& name) const;
	bool removePort(const std::string& name) const;
	State waitForStop();

	void stoppingFunction(StopFunctionType stop);
	void handleStopImpl(StopFunctionType function);

	std::string m_name;
	int m_id;
	bool m_managed;

	std::string m_starterEndpoint;
	std::string m_starterName;
	int m_starterId;

	std::unique_ptr<Server> m_server;
	std::unique_ptr<Server> m_starterServer;

	std::unique_ptr<WaitingImplSet> m_waitingSet;
	std::unique_ptr<HandlerImpl> m_stopHandler;

	static This m_instance;
	static const std::string RUNNING_STATE;
};


class Instance : private EventListener {

	friend class cameo::Server;
	friend class cameo::application::Subscriber;
	friend std::ostream& operator<<(std::ostream&, const Instance&);

public:
	typedef std::function<void (State)> StateHandlerType;

	~Instance();

	const std::string& getName() const;
	int getId() const;
	const std::string& getUrl() const;
	const std::string& getEndpoint() const;
	std::string getNameId() const;
	bool hasResult() const;
	bool exists() const;
	const std::string& getErrorMessage() const;
	bool stop();
	bool kill();

	State waitFor(StateHandlerType handler = nullptr);
	State waitFor(int states, StateHandlerType handler = nullptr);
	State waitFor(const std::string& eventName);
	State waitFor(KeyValue& keyValue);

	void cancelWaitFor();

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

	bool getBinaryResult(std::string& result);
	bool getResult(std::string& result);

	std::shared_ptr<OutputStreamSocket> getOutputStreamSocket();

	std::string getKeyValue(const std::string& key);

private:
	Instance(Server * server);

	void setId(int id);
	void setErrorMessage(const std::string& message);
	void setOutputStreamSocket(std::unique_ptr<OutputStreamSocket>& socket);
	void setPastStates(State pastStates);
	void setInitialState(State state);
	State waitFor(int states, const std::string& eventName, KeyValue& keyValue, StateHandlerType handler, bool blocking);

	Server * m_server;
	std::shared_ptr<OutputStreamSocket> m_outputStreamSocket;
	int m_id;
	std::string m_errorMessage;
	int m_pastStates;
	State m_initialState;
	State m_lastState;
	bool m_hasResult;
	std::string m_resultData;
	int m_exitCode;
	std::unique_ptr<WaitingImpl> m_waiting;
};

///////////////////////////////////////////////////////////////////////////
// InstanceArray

class InstanceArray {

	friend class cameo::Server;

public:
	InstanceArray(const InstanceArray& array);
	~InstanceArray();

	std::size_t size() const;
	std::unique_ptr<Instance>& operator[](std::size_t index);

private:
	InstanceArray();
	void allocate(std::size_t size);

	std::size_t m_size;
	std::unique_ptr<Instance>* m_array;
};

///////////////////////////////////////////////////////////////////////////
// Publisher

class Publisher {

	friend class cameo::application::This;
	friend std::ostream& operator<<(std::ostream&, const Publisher&);

public:
	~Publisher();

	/**
	 * Returns the publisher with name.
	 * throws PublisherCreationException.
	 */
	static std::unique_ptr<Publisher> create(const std::string& name, int numberOfSubscribers = 0);

	const std::string& getName() const;
	const std::string& getApplicationName() const;
	int getApplicationId() const;
	const std::string& getApplicationEndpoint() const;

	/**
	 * Returns true if the wait succeeds or false if it was canceled.
	 */
	bool waitForSubscribers() const;
	void cancelWaitForSubscribers();

	void sendBinary(const std::string& data) const;
	void send(const std::string& data) const;
	void sendTwoBinaryParts(const std::string& data1, const std::string& data2) const;
	void sendEnd() const;

	/**
	 * Deprecated.
	 * TODO remove in next version.
	 */
	bool hasEnded() const;

	bool isEnded() const;

private:
	Publisher(application::This * application, int publisherPort, int synchronizerPort, const std::string& name, int numberOfSubscribers);

	std::unique_ptr<PublisherImpl> m_impl;
	std::unique_ptr<WaitingImpl> m_waiting;
};

///////////////////////////////////////////////////////////////////////////
// Subscriber

class Subscriber {

	friend class cameo::Server;
	friend class cameo::application::Instance;
	friend std::ostream& operator<<(std::ostream&, const Subscriber&);

public:
	~Subscriber();

	static std::unique_ptr<Subscriber> create(Instance & instance, const std::string& publisherName);

	const std::string& getPublisherName() const;
	const std::string& getInstanceName() const;
	int getInstanceId() const;
	const std::string& getInstanceEndpoint() const;

	/**
	 * Deprecated.
	 * TODO remove in next version.
	 */
	bool hasEnded() const;

	bool isEnded() const;
	bool isCanceled() const;

	/**
	 * Returns false if the stream finishes.
	 */
	bool receiveBinary(std::string& data) const;
	bool receive(std::string& data) const;
	bool receiveTwoBinaryParts(std::string& data1, std::string& data2) const;

	void cancel();

private:
	Subscriber(Server * server, const std::string& url, int publisherPort, int synchronizerPort, const std::string& publisherName, int numberOfSubscribers, const std::string& instanceName, int instanceId, const std::string& instanceEndpoint, const std::string& statusEndpoint);
	void init();

	std::unique_ptr<SubscriberImpl> m_impl;
	std::unique_ptr<WaitingImpl> m_waiting;
};

///////////////////////////////////////////////////////////////////////////
// Request

class Request {

	friend class cameo::application::Responder;
	friend std::ostream& operator<<(std::ostream&, const Request&);

public:
	~Request();

	std::string getObjectId() const;
	std::string getRequesterEndpoint() const;

	const std::string& getBinary() const;
	std::string get() const;
	const std::string& getSecondBinaryPart() const;

	void setTimeout(int value);

	bool replyBinary(const std::string& response);
	bool reply(const std::string& response);

	std::unique_ptr<Instance> connectToRequester();

	/**
	 * Transfers the ownership of the requester server.
	 */
	std::unique_ptr<Server> getServer();

private:
	Request(std::unique_ptr<RequestImpl> & impl);

	std::unique_ptr<RequestImpl> m_impl;
	std::unique_ptr<Server> m_requesterServer;
};

///////////////////////////////////////////////////////////////////////////
// Responder

class Responder {

	friend std::ostream& operator<<(std::ostream&, const Responder&);

public:
	~Responder();

	/**
	 * Returns the responder with name.
	 * throws ResponderCreationException.
	 */
	static std::unique_ptr<Responder> create(const std::string& name);

	const std::string& getName() const;

	void cancel();
	std::unique_ptr<Request> receive();

	bool isCanceled() const;

private:
	Responder(application::This * application, int responderPort, const std::string& name);

	std::unique_ptr<ResponderImpl> m_impl;
	std::unique_ptr<WaitingImpl> m_waiting;
};

///////////////////////////////////////////////////////////////////////////
// Requester

class Requester {

friend std::ostream& operator<<(std::ostream&, const Requester&);

public:
	~Requester();

	/**
	 * Returns the responder with name.
	 * throws RequesterCreationException.
	 */
	static std::unique_ptr<Requester> create(Instance & instance, const std::string& name);

	const std::string& getName() const;

	void sendBinary(const std::string& request);
	void send(const std::string& request);
	void sendTwoBinaryParts(const std::string& request1, const std::string& request2);

	bool receiveBinary(std::string& response);
	bool receive(std::string& response);

	void cancel();

	bool isCanceled() const;

private:
	Requester(application::This * application, const std::string& url, int requesterPort, int responderPort, const std::string& name, int responderId, int requesterId);

	std::unique_ptr<RequesterImpl> m_impl;
	std::unique_ptr<WaitingImpl> m_waiting;
};

///////////////////////////////////////////////////////////////////////////
// Configuration

class Configuration {

	friend std::ostream& operator<<(std::ostream&, const Configuration&);

public:
	Configuration(const std::string& name, const std::string& description, bool singleInfo, bool restart, int startingTime, int stoppingTime);

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
	Info(const std::string& name, int id, int pid, State applicationState, State pastApplicationStates, const std::string& args);

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

std::string toString(cameo::application::State applicationStates);
std::ostream& operator<<(std::ostream&, const cameo::application::This&);
std::ostream& operator<<(std::ostream&, const cameo::application::Instance&);
std::ostream& operator<<(std::ostream&, const cameo::application::Publisher&);
std::ostream& operator<<(std::ostream&, const cameo::application::Subscriber&);
std::ostream& operator<<(std::ostream&, const cameo::application::Request&);
std::ostream& operator<<(std::ostream&, const cameo::application::Responder&);
std::ostream& operator<<(std::ostream&, const cameo::application::Requester&);
std::ostream& operator<<(std::ostream&, const cameo::application::Configuration&);
std::ostream& operator<<(std::ostream&, const cameo::application::Info&);

}
}


#endif
