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

#ifndef CAMEO_SUBSCRIBERZMQ_H_
#define CAMEO_SUBSCRIBERZMQ_H_

#include "../SubscriberImpl.h"
#include <zmq.hpp>

namespace cameo {

class Server;

namespace coms {

class SubscriberZmq : public SubscriberImpl {

public:
	SubscriberZmq();
	virtual ~SubscriberZmq();

	virtual void init(int publisherPort, int synchronizerPort, const std::string& publisherName, int numberOfSubscribers, application::Instance & instance);

	virtual const std::string& getPublisherName() const;
	virtual const std::string& getInstanceName() const;
	virtual int getInstanceId() const;
	virtual Endpoint getInstanceEndpoint() const;

	virtual bool isEnded() const;
	virtual bool isCanceled() const;

	virtual std::optional<std::string> receiveBinary();
	virtual std::optional<std::string> receive();
	virtual std::optional<std::tuple<std::string, std::string>> receiveTwoBinaryParts();

	virtual void cancel();

private:
	std::string createSubscribePublisherRequest() const;

	Endpoint m_serverEndpoint;
	int m_publisherPort;
	int m_synchronizerPort;
	std::string m_publisherName;
	int m_numberOfSubscribers;
	std::string m_instanceName;
	int m_instanceId;
	std::string m_statusEndpoint;
	std::unique_ptr<zmq::socket_t> m_subscriber;
	std::string m_cancelEndpoint;
	std::unique_ptr<zmq::socket_t> m_cancelPublisher;
	bool m_ended;
	bool m_canceled;
};

}
}

#endif
