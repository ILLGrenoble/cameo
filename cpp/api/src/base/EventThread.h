/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
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