/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

#ifndef CAMEO_COMS_REQUESTERZMQ_H_
#define CAMEO_COMS_REQUESTERZMQ_H_

#include "../RequesterImpl.h"
#include <atomic>
#include <zmq.hpp>

namespace cameo {

class ContextZmq;

namespace coms {

class RequesterZmq : public RequesterImpl {

public:
	RequesterZmq();
	virtual ~RequesterZmq();

	virtual void setPollingTime(int value);
	virtual void setTimeout(int value);

	virtual void init(const Endpoint& endpoint, const std::string& responderIdentity, const TimeoutCounter& timeoutCounter);
	virtual void send(const std::string& requestData);
	virtual void sendTwoParts(const std::string& requestData1, const std::string& requestData2);

	virtual std::optional<std::string> receive();

	virtual void cancel();
	virtual bool isCanceled();

	virtual bool hasTimedout();

private:
	void resetSocket();
	void createSocket();
	void createAndSyncSocket(const TimeoutCounter& timeoutCounter);
	bool initSocket(const TimeoutCounter& timeoutCounter);
	bool initSocketForSend();
	bool sendSync();
	void sendRequest(const std::string& request);
	void sendRequest(const std::string& requestPart1, const std::string& requestPart2);
	void sendRequest(const std::string& requestPart1, const std::string& requestPart2, const std::string& requestPart3);
	bool receiveMessage(zmq::message_t& message);

	int m_pollingTime;
	int m_timeout;
	ContextZmq* m_contextImpl;
	std::unique_ptr<zmq::socket_t> m_requester;
	Endpoint m_endpoint;
	std::string m_responderIdentity;
	std::atomic_bool m_canceled;
	std::atomic_bool m_timedout;
};

}
}

#endif
