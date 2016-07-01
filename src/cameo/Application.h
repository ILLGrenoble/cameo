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

#include <boost/function.hpp>
#include <boost/bind.hpp>
#include <vector>
#include <set>
#include <memory>
#include <stdint.h>
#include "InvalidArgumentException.h"
#include "SocketException.h"
#include "ConnectionTimeout.h"
#include "PublisherCreationException.h"
#include "RequesterCreationException.h"
#include "ResponderCreationException.h"
#include "Response.h"
#include "Services.h"

namespace cameo {

enum Option {
	NONE = 0
};

class Server;
class EventStreamSocket;
class ApplicationImpl;
class PublisherImpl;
class SubscriberImpl;
class RequestImpl;
class ResponderImpl;
class RequesterImpl;
class WaitingImpl;
class SocketWaitingImpl;
class GenericWaitingImpl;
class WaitingImplSet;

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
const State ERROR = 32;
const State SUCCESS = 64;
const State STOPPED = 128;
const State KILLED = 256;


class This : private Services {

	friend class cameo::application::Publisher;
	friend class cameo::application::Responder;
	friend class cameo::application::Requester;
	friend class cameo::PublisherImpl;
	friend class cameo::RequestImpl;
	friend class cameo::ResponderImpl;
	friend class cameo::RequesterImpl;
	friend class cameo::ApplicationImpl;
	friend class cameo::SocketWaitingImpl;
	friend class cameo::GenericWaitingImpl;
	friend class cameo::Server;
	friend std::ostream& operator<<(std::ostream&, const cameo::application::This&);

	typedef boost::function<void ()> StopFunctionType;

public:
	static void init(int argc, char *argv[]);
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
		m_instance->handleStopImpl(function);
	}

	static void cancelWaitings();

	static bool setRunning();

	/**
	 * Sets the result.
	 */
	static void setBinaryResult(const std::string& data);
	static void setResult(const std::string& data);
	static void setResult(const int32_t* data, std::size_t size);
	static void setResult(const int64_t* data, std::size_t size);
	static void setResult(const float* data, std::size_t size);
	static void setResult(const double* data, std::size_t size);

	/**
	 * Connects to the starter application, i.e. the application which started this application.
	 */
	static std::auto_ptr<Instance> connectToStarter();

private:
	This(int argc, char *argv[]);
	~This();
	void init();
	static std::string getReference();
	static State parseState(const std::string& value);
	State getState(int id) const;
	bool destroyPublisher(const std::string& name) const;
	bool removePort(const std::string& name) const;
	State waitForStop();
	void handleStopImpl(StopFunctionType function);

	ApplicationImpl * m_impl;
	std::string m_name;
	int m_id;

	std::string m_starterEndpoint;
	std::string m_starterName;
	int m_starterId;

	std::auto_ptr<Server> m_server;
	std::auto_ptr<Server> m_starterServer;

	std::auto_ptr<WaitingImplSet> m_waitingSet;

	static This * m_instance;
	static const std::string RUNNING_STATE;
};


class Instance {

	friend class cameo::Server;
	friend class cameo::application::Subscriber;
	friend std::ostream& operator<<(std::ostream&, const Instance&);

public:
	~Instance();

	const std::string& getName() const;
	int getId() const;
	const std::string& getUrl() const;
	const std::string& getEndpoint() const;
	std::string getNameId() const;
	bool hasResult() const;
	bool exists() const;
	const std::string& getErrorMessage() const;
	State getInitialState() const;
	bool stop();
	bool kill();
	State waitFor(int states = 0);
	State waitFor(int states, const std::string& eventName);
	void cancelWaitFor();

	bool getBinaryResult(std::string& result);
	bool getResult(std::string& result);
	bool getResult(std::vector<int32_t>& result);
	bool getResult(std::vector<int64_t>& result);
	bool getResult(std::vector<float>& result);
	bool getResult(std::vector<double>& result);

private:
	Instance(const Server * server, std::auto_ptr<EventStreamSocket>& socket);

	void setId(int id);
	void setName(const std::string& name);
	void setErrorMessage(const std::string& message);
	void setPastStates(State pastStates);
	void setInitialState(State state);

	const Server * m_server;
	std::auto_ptr<EventStreamSocket> m_eventSocket;
	std::auto_ptr<WaitingImpl> m_waiting;
	std::string m_name;
	int m_id;
	std::string m_errorMessage;
	int m_pastStates;
	State m_initialState;
	State m_lastState;
	bool m_hasResult;
	std::string m_resultData;
};

///////////////////////////////////////////////////////////////////////////
// InstanceArray

class InstanceArray {

	friend class cameo::Server;

public:
	InstanceArray(const InstanceArray& array);
	~InstanceArray();

	std::size_t size() const;
	std::auto_ptr<Instance>& operator[](std::size_t index);

private:
	InstanceArray();
	void allocate(std::size_t size);

	std::size_t m_size;
	std::auto_ptr<Instance>* m_array;
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
	static std::auto_ptr<Publisher> create(const std::string& name, int numberOfSubscribers = 0);

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
	void send(const int32_t* data, std::size_t size) const;
	void send(const int64_t* data, std::size_t size) const;
	void send(const float* data, std::size_t size) const;
	void send(const double* data, std::size_t size) const;
	void sendEnd() const;
	bool hasEnded() const;

private:
	Publisher(const application::This * application, int publisherPort, int synchronizerPort, const std::string& name, int numberOfSubscribers);

	std::auto_ptr<PublisherImpl> m_impl;
	std::auto_ptr<WaitingImpl> m_waiting;
};

///////////////////////////////////////////////////////////////////////////
// Subscriber

class Subscriber {

	friend class cameo::Server;
	friend class cameo::application::Instance;
	friend std::ostream& operator<<(std::ostream&, const Subscriber&);

public:
	~Subscriber();

	static std::auto_ptr<Subscriber> create(Instance & instance, const std::string& publisherName);

	const std::string& getPublisherName() const;
	const std::string& getInstanceName() const;
	int getInstanceId() const;
	const std::string& getInstanceEndpoint() const;

	bool hasEnded() const;

	/**
	 * Returns false if the stream finishes.
	 */
	bool receiveBinary(std::string& data) const;
	bool receive(std::string& data) const;
	bool receive(std::vector<int32_t>& data) const;
	bool receive(std::vector<int64_t>& data) const;
	bool receive(std::vector<float>& data) const;
	bool receive(std::vector<double>& data) const;

	void cancel();

private:
	Subscriber(const Server * server, const std::string& url, int publisherPort, int synchronizerPort, const std::string& publisherName, int numberOfSubscribers, const std::string& instanceName, int instanceId, const std::string& instanceEndpoint, const std::string& statusEndpoint);
	void init();

	std::auto_ptr<SubscriberImpl> m_impl;
	std::auto_ptr<WaitingImpl> m_waiting;
};

///////////////////////////////////////////////////////////////////////////
// Request

class Request {

	friend class cameo::application::Responder;
	friend std::ostream& operator<<(std::ostream&, const Request&);

public:
	~Request();

	const std::string& getBinaryData() const;
	std::string getData() const;

	void replyBinary(const std::string& response);
	void reply(const std::string& response);

private:
	Request(std::auto_ptr<RequestImpl> & impl);

	std::auto_ptr<RequestImpl> m_impl;
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
	static std::auto_ptr<Responder> create(const std::string& name);

	const std::string& getName() const;

	void cancel();
	std::auto_ptr<Request> receive();
	bool hasEnded() const;

private:
	Responder(const application::This * application, int responderPort, const std::string& name);

	std::auto_ptr<ResponderImpl> m_impl;
	std::auto_ptr<WaitingImpl> m_waiting;
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
	static std::auto_ptr<Requester> create(Instance & instance, const std::string& name);

	const std::string& getName() const;

	void sendBinary(const std::string& request);
	void send(const std::string& request);

	bool receiveBinary(std::string& response);
	bool receive(std::string& response);

	void cancel();

private:
	Requester(const application::This * application, const std::string& url, int requesterPort, int responderPort, const std::string& name);

	std::auto_ptr<RequesterImpl> m_impl;
	std::auto_ptr<WaitingImpl> m_waiting;
};

///////////////////////////////////////////////////////////////////////////
// Configuration

class Configuration {

	friend std::ostream& operator<<(std::ostream&, const Configuration&);

public:
	Configuration(const std::string& name, const std::string& description, bool singleInfo, bool restart, int startingTime, int retries, int stoppingTime);

	const std::string& getName() const;
	const std::string& getDescription() const;
	bool hasSingleInstance() const;
	bool canRestart() const;
	int getStartingTime() const;
	int getRetries() const;
	int getStoppingTime() const;

private:
	std::string m_name;
	std::string m_description;
	bool m_singleInstance;
	bool m_restart;
	int m_startingTime;
	int m_retries;
	int m_stoppingTime;
};

///////////////////////////////////////////////////////////////////////////
// Info

class Info {

	friend std::ostream& operator<<(std::ostream&, const Info&);

public:
	Info(const std::string& name, int id, State applicationState, State pastApplicationStates, const std::string& args);

	int getId() const;
	State getState() const;
	State getPastStates() const;
	const std::string& getArgs() const;
	const std::string& getName() const;

private:
	int m_id;
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
