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

#include <string>
#include <vector>
#include <stdint.h>

#include "SocketWaitingImpl.h"
#include "zmq.hpp"

namespace cameo {

class Server;

class SubscriberImpl {

public:
	SubscriberImpl(const Server * server, const std::string & url, int publisherPort, int synchronizerPort, const std::string& publisherName, int numberOfSubscribers, const std::string& instanceName, int instanceId, const std::string& instanceEndpoint, const std::string& statusEndpoint);
	~SubscriberImpl();

	void init();
	bool hasEnded() const;
	bool receiveBinary(std::string& data);
	bool receive(std::string& data);
	bool receive(std::vector<int32_t>& data);
	bool receive(std::vector<int64_t>& data);
	bool receive(std::vector<float>& data);
	bool receive(std::vector<double>& data);
	bool receiveTwoBinaryParts(std::string& data1, std::string& data2);

	WaitingImpl * waiting();

	const Server * m_server;
	std::string m_url;
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
	bool m_endOfStream;

	static const std::string SYNC;
	static const std::string STREAM;
	static const std::string ENDSTREAM;
	static const std::string CANCEL;
	static const std::string STATUS;
};

}

#endif
