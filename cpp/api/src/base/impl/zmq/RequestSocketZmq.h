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

#ifndef CAMEO_REQUESTSOCKETZMQ_H_
#define CAMEO_REQUESTSOCKETZMQ_H_

#include "../RequestSocketImpl.h"
#include <memory>
#include <zmq.hpp>

namespace cameo {

class Context;
class ContextZmq;

class RequestSocketZmq : public RequestSocketImpl {

public:
	RequestSocketZmq(Context * context, const std::string& endpoint);
	virtual ~RequestSocketZmq();

	virtual void setTimeout(int timeout);

	virtual std::string request(const std::string& request, int overrideTimeout);
	virtual std::string request(const std::string& requestPart1, const std::string& requestPart2, int overrideTimeout);
	virtual std::string request(const std::string& requestPart1, const std::string& requestPart2, const std::string& requestPart3, int overrideTimeout);

	ContextZmq * m_context;
	std::string m_endpoint;
	std::unique_ptr<zmq::socket_t> m_socket;
	int m_timeout;

private:
	void setSocketLinger();
	void init();
	void reset();

	std::unique_ptr<zmq::message_t> receive(int overrideTimeout);
};

}

#endif
