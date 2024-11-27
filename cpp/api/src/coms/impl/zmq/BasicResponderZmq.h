/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

#ifndef CAMEO_COMS_BASIC_RESPONDERZMQ_H_
#define CAMEO_COMS_BASIC_RESPONDERZMQ_H_

#include "../BasicResponderImpl.h"
#include "../../../base/JSON.h"
#include <zmq.hpp>
#include <atomic>

namespace cameo {
namespace coms {
namespace basic {

class Request;

class ResponderZmq : public ResponderImpl {

public:
	ResponderZmq();
	~ResponderZmq();

	virtual void init(const std::string& responderIdentity);
	virtual int getResponderPort();
	virtual void cancel();
	virtual bool isCanceled();

	virtual std::unique_ptr<Request> receive();
	virtual void reply(const std::string& type, const std::string& response);

private:
	void replyOK();
	std::unique_ptr<Request> processCancel();
	std::unique_ptr<Request> processRequest(const json::Object& jsonRequest);

	void terminate();

	int m_responderPort;
	std::unique_ptr<zmq::socket_t> m_responder;
	std::string m_responderIdentity;

	static constexpr int HEADER_SIZE = 4;
	std::string m_requestHeader[HEADER_SIZE];

	std::atomic_bool m_canceled;
};

}
}
}

#endif