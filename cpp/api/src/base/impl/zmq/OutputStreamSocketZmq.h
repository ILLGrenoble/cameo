/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

#ifndef CAMEO_OUTPUTSTREAMSOCKETZMQ_H_
#define CAMEO_OUTPUTSTREAMSOCKETZMQ_H_

#include "../StreamSocketImpl.h"
#include <string>
#include <memory>
#include <zmq.hpp>

namespace cameo {

class Server;
class ContextZmq;

class OutputStreamSocketZmq : public StreamSocketImpl {

public:
	OutputStreamSocketZmq(const std::string& name);
	virtual ~OutputStreamSocketZmq();

	void init(Context * context, const Endpoint& endpoint, RequestSocket * requestSocket);
	void send(const std::string& data);
	std::string receive(bool blocking);
	void cancel();
	void terminate();

private:
	std::string m_name;
	ContextZmq * m_context;
	std::unique_ptr<zmq::socket_t> m_socket;
	std::unique_ptr<zmq::socket_t> m_cancelSocket;
};

}

#endif