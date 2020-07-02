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

#ifndef CAMEO_REQUESTERIMPL_H_
#define CAMEO_REQUESTERIMPL_H_

#include <string>
#include <vector>
#include <thread>
#include <mutex>
#include "GenericWaitingImpl.h"
#include "zmq.hpp"

namespace cameo {

namespace application {
	class This;
}

class RequestSocketImpl;

class RequesterImpl {

public:
	RequesterImpl(application::This * application, const std::string& url, int requesterPort, int responderPort, const std::string& name, int responderId, int requesterId);
	~RequesterImpl();

	static int newRequesterId();
	static std::string getRequesterPortName(const std::string& name, int responderId, int requesterId);

	WaitingImpl * waiting();

	void sendBinary(const std::string& request);
	void send(const std::string& request);
	void sendTwoBinaryParts(const std::string& request1, const std::string& request2);

	bool receiveBinary(std::string& response);
	bool receive(std::string& response);

	void cancel();
	void terminate();

	application::This * m_application;
	int m_requesterPort;
	std::string m_name;
	int m_responderId;
	int m_requesterId;
	std::string m_responderEndpoint;
	std::unique_ptr<RequestSocketImpl> m_requestSocket;
	std::unique_ptr<zmq::socket_t> m_repSocket;
	bool m_canceled;

	static const std::string REQUESTER_PREFIX;

	static std::mutex m_mutex;
	static int m_requesterCounter;
};

}

#endif
