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

namespace application {

class Instance;

}

class EventStreamSocket {

	friend class Server;
	friend class application::Instance;

public:
	~EventStreamSocket();

	std::unique_ptr<Event> receive(bool blocking = true);
	void cancel();

private:
	EventStreamSocket();
	void init(Context * context, const Endpoint& endpoint, RequestSocket * requestSocket);

	std::unique_ptr<StreamSocketImpl> m_impl;
};

}

#endif