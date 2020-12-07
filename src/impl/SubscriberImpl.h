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

#ifndef CAMEO_SUBSCRIBERIMPL_H_
#define CAMEO_SUBSCRIBERIMPL_H_

#include "SocketWaitingImpl.h"
#include "zmq.hpp"
#include <string>
#include <vector>
#include <optional>
#include <tuple>

namespace cameo {

class Server;

class SubscriberImpl {

public:
	SubscriberImpl(Server * server, int publisherPort, int synchronizerPort, const std::string& publisherName, int numberOfSubscribers, const std::string& instanceName, int instanceId, const std::string& instanceEndpoint, const std::string& statusEndpoint);
	~SubscriberImpl();

	void init();

	bool isEnded() const;
	bool isCanceled() const;

	std::optional<std::string> receiveBinary();
	std::optional<std::string> receive();
	std::optional<std::tuple<std::string, std::string>> receiveTwoBinaryParts();

	WaitingImpl * waiting();

	Server * m_server;
	int m_publisherPort;
	int m_synchronizerPort;
	std::string m_publisherName;
	int m_numberOfSubscribers;
	std::string m_instanceName;
	int m_instanceId;
	std::string m_instanceEndpoint;
	std::string m_statusEndpoint;
	std::unique_ptr<zmq::socket_t> m_subscriber;
	std::string m_cancelEndpoint;
	std::unique_ptr<zmq::socket_t> m_cancelPublisher;
	bool m_ended;
	bool m_canceled;
};

}

#endif
