/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

#ifndef CAMEO_SUBSCRIBERZMQ_H_
#define CAMEO_SUBSCRIBERZMQ_H_

#include "../SubscriberImpl.h"
#include <zmq.hpp>
#include <atomic>

namespace cameo {

class Server;

namespace coms {

class SubscriberZmq : public SubscriberImpl {

public:
	SubscriberZmq();
	virtual ~SubscriberZmq();

	virtual void setPollingTime(int value);
	virtual void setTimeout(int value);

	virtual void init(int appId, const Endpoint& endpoint, const Endpoint& appStatusEndpoint, const std::string& publisherIdentity, bool checkApp);
	virtual bool sync(int timeout);

	virtual bool hasEnded() const;
	virtual bool isCanceled() const;

	virtual bool hasTimedout();

	virtual std::optional<std::string> receive();
	virtual std::optional<std::tuple<std::string, std::string>> receiveTwoParts();

	virtual void cancel();

private:
	bool receiveMessage(zmq::message_t& message);

	int m_appId;
	int m_pollingTime;
	int m_timeout;
	std::unique_ptr<zmq::socket_t> m_subscriber;
	std::string m_publisherIdentity;
	std::string m_cancelEndpoint;
	std::unique_ptr<zmq::socket_t> m_cancelPublisher;
	std::atomic_bool m_ended;
	std::atomic_bool m_canceled;
	std::atomic_bool m_timedout;
	zmq_pollitem_t m_items[1];
};

}
}

#endif