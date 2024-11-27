/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

#ifndef CAMEO_EVENTSTREAMSOCKET_H_
#define CAMEO_EVENTSTREAMSOCKET_H_

#include "Event.h"
#include "Strings.h"
#include <memory>

namespace cameo {

class Context;
class RequestSocket;
class StreamSocketImpl;
class App;

/**
 * Class defining an event stream socket.
 */
class CAMEO_EXPORT EventStreamSocket {

	friend class Server;
	friend class App;

public:
	/**
	 * Destructor.
	 */
	~EventStreamSocket();

	/**
	 * Terminates the communication.
	 */
	void terminate();

	/**
	 * Receives the event.
	 * \param blocking True if the call is blocking.
	 */
	std::unique_ptr<Event> receive(bool blocking = true);

	/**
	 * Cancels the socket. Any waiting receive() call is unblocked.
	 */
	void cancel();

private:
	EventStreamSocket();
	void init(Context * context, const Endpoint& endpoint, RequestSocket * requestSocket);

	std::unique_ptr<StreamSocketImpl> m_impl;
};

}

#endif