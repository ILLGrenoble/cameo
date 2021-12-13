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

#ifndef CAMEO_EVENTSTREAMSOCKETIMPL_H_
#define CAMEO_EVENTSTREAMSOCKETIMPL_H_

#include <string>
#include <memory>
#include "Strings.h"
#include <zmq.hpp>

namespace cameo {

class Server;
class ContextZmq;

class EventStreamSocketImpl {

public:
	EventStreamSocketImpl(Server * server);
	virtual ~EventStreamSocketImpl();

	void init();
	void send(const std::string& data);
	std::string receive(bool blocking = true);
	void cancel();
	void close();

	Server * m_server;
	ContextZmq * m_context;
	std::unique_ptr<zmq::socket_t> m_socket;
	std::unique_ptr<zmq::socket_t> m_cancelSocket;
};

}

#endif
