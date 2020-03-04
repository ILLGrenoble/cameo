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

#ifndef CAMEO_STREAMSOCKETIMPL_H_
#define CAMEO_STREAMSOCKETIMPL_H_

#include <string>
#include <memory>
#include "zmq.hpp"

namespace cameo {

class StreamSocketImpl {

public:
	StreamSocketImpl(zmq::socket_t * socket, zmq::socket_t * cancelSocket = nullptr);
	virtual ~StreamSocketImpl();

	void send(const std::string& data);
	std::unique_ptr<zmq::message_t> receive(bool blocking = true);
	void cancel();
	void close();

	std::unique_ptr<zmq::socket_t> m_socket;
	std::unique_ptr<zmq::socket_t> m_cancelSocket;

	static const std::string CANCEL;
};

}

#endif
