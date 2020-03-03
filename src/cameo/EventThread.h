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
class StatusEvent;
class ResultEvent;
class PublisherEvent;
class PortEvent;

class EventThread {

public:
	EventThread(Server * server, std::unique_ptr<EventStreamSocket>& socket);
	~EventThread();

	void start();
	void cancel();

private:
	void processStatusEvent(StatusEvent * status);
	void processResultEvent(ResultEvent * result);
	void processPublisherEvent(PublisherEvent * publisher);
	void processPortEvent(PortEvent * port);

	Server * m_server;
	std::unique_ptr<EventStreamSocket> m_socket;
	std::unique_ptr<std::thread> m_thread;

};

}

#endif
