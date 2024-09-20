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
class EventStreamSocket {

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
