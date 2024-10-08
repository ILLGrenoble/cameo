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

#ifndef CAMEO_EVENTTHREAD_H_
#define CAMEO_EVENTTHREAD_H_

#include <string>
#include <memory>
#include <thread>

namespace cameo {

class Server;
class EventStreamSocket;

/**
 * Class defining an event thread.
 */
class EventThread {

public:
	/**
	 * Constructor.
	 * \param server The server.
	 * \param socket The socket.
	 */
	EventThread(Server * server, std::unique_ptr<EventStreamSocket>& socket);

	/**
	 * Destructor.
	 */
	~EventThread();

	/**
	 * Starts the event thread.
	 */
	void start();

	/**
	 * Cancels the event thread.
	 */
	void cancel();

private:
	Server * m_server;
	std::unique_ptr<EventStreamSocket> m_socket;
	std::unique_ptr<std::thread> m_thread;
};

}

#endif
