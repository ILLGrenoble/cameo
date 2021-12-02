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

#ifndef CAMEO_SOCKETWAITINGIMPL_H_
#define CAMEO_SOCKETWAITINGIMPL_H_

#include "WaitingImpl.h"
#include "zmq.hpp"
#include <string>
#include <memory>

namespace cameo {

class SocketWaitingImpl : public WaitingImpl {

public:
	SocketWaitingImpl(zmq::socket_t* socket, const std::string& message);
	virtual ~SocketWaitingImpl();

	virtual void cancel();

	zmq::socket_t* m_socket;
	std::string m_message;
};

}

#endif
