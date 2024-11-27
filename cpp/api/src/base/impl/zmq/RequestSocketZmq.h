/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
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
	RequestSocketZmq(Context * context, const std::string& endpoint, const std::string& responderIdentity);
	virtual ~RequestSocketZmq();

	virtual void setTimeout(int timeout);

	virtual std::string request(const std::string& request, int overrideTimeout);
	virtual std::string request(const std::string& requestPart1, const std::string& requestPart2, int overrideTimeout);
	virtual std::string request(const std::string& requestPart1, const std::string& requestPart2, const std::string& requestPart3, int overrideTimeout);

private:
	void setSocketLinger();
	void init();
	void reset();
	std::unique_ptr<zmq::message_t> receive(int overrideTimeout);

	ContextZmq * m_context;
	std::string m_endpoint;
	std::string m_responderIdentity;
	std::unique_ptr<zmq::socket_t> m_socket;
	int m_timeout;
};

}

#endif