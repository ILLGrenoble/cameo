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

#ifndef CAMEO_REQUESTSOCKETIMPL_H_
#define CAMEO_REQUESTSOCKETIMPL_H_

#include <string>
#include <memory>
#include "zmq.hpp"

namespace cameo {

class RequestSocketImpl {

public:
	RequestSocketImpl(zmq::socket_t * socket, int timeout = 0);
	virtual ~RequestSocketImpl();

	void setTimeout(int timeout);

	std::unique_ptr<zmq::message_t> request(const std::string& requestTypePart, const std::string& requestDataPart, int overrideTimeout = -1);

	std::unique_ptr<zmq::socket_t> m_socket;
	int m_timeout;
};

}

#endif
