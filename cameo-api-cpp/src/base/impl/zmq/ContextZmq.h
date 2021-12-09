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

#ifndef CAMEO_CONTEXTZMQ_H_
#define CAMEO_CONTEXTZMQ_H_

#include "Context.h"
#include <vector>
#include <memory>

namespace zmq {

	class socket_t;
	class context_t;
}

namespace cameo {

class RequestSocket;

class ContextZmq : public Context {

public:
	ContextZmq();
	virtual ~ContextZmq();

	void setTimeout(int timeout);
	int getTimeout() const;

	zmq::context_t& getContext();

	zmq::socket_t * createEventSubscriber(const std::string& endpoint, const std::string& cancelEndpoint);
	zmq::socket_t * createOutputStreamSubscriber(const std::string& endpoint, const std::string& cancelEndpoint);
	zmq::socket_t * createCancelPublisher(const std::string& endpoint);
	zmq::socket_t * createRequestSocket(const std::string& endpoint);

	bool isAvailable(RequestSocket * socket, int timeout);
	void sendSyncStream(RequestSocket * socket, const std::string& name);
	void waitForStreamSubscriber(zmq::socket_t * subscriber, RequestSocket * socket, const std::string& name);
	void waitForSubscriber(zmq::socket_t * subscriber, RequestSocket * socket);

private:
	std::unique_ptr<zmq::context_t> m_context;
	int m_timeout;
};

}

#endif
