/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

#ifndef CAMEO_COMS_MULTI_RESPONDERROUTERZMQ_H_
#define CAMEO_COMS_MULTI_RESPONDERROUTERZMQ_H_

#include "../MultiResponderRouterImpl.h"
#include <zmq.hpp>
#include <atomic>

namespace cameo {
namespace coms {
namespace multi {

class Request;

class ResponderRouterZmq : public multi::ResponderRouterImpl {

public:
	ResponderRouterZmq();
	~ResponderRouterZmq();

	virtual void init(const std::string& responderIdentity, const std::string& dealerEndpoint);
	virtual void setPollingTime(int value);
	virtual int getResponderPort();
	virtual void cancel();
	virtual bool isCanceled();
	virtual void run();

private:
	void terminate();

	int m_pollingTime;
	int m_responderPort;
	std::unique_ptr<zmq::socket_t> m_router;
	std::unique_ptr<zmq::socket_t> m_dealer;

	std::atomic_bool m_canceled;
};

}
}
}

#endif