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

#include <memory>
#include "Event.h"

namespace cameo {

class SocketImpl;
class WaitingImpl;

namespace application {

class Instance;

}

class EventStreamSocket {

	friend class Services;
	friend class application::Instance;

public:
	~EventStreamSocket();

	std::auto_ptr<Event> receive(bool blocking = true);
	void cancel();

private:
	EventStreamSocket(SocketImpl * impl);

	WaitingImpl * waiting();

	std::auto_ptr<SocketImpl> m_impl;
};

}

#endif
