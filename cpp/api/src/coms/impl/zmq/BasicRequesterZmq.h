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

#ifndef CAMEO_COMS_BASIC_REQUESTERZMQ_H_
#define CAMEO_COMS_BASIC_REQUESTERZMQ_H_

#include "../BasicRequesterImpl.h"
#include "../../../base/Waiting.h"
#include "Strings.h"
#include <string>
#include <optional>
#include <atomic>
#include <zmq.hpp>

namespace cameo {
namespace coms {
namespace basic {

class RequesterZmq : public RequesterImpl {

public:
	RequesterZmq();
	virtual ~RequesterZmq();

	virtual void setPollingTime(int value);
	virtual void setTimeout(int value);

	virtual void init(const Endpoint& endpoint, int responderPort);
	virtual void sendBinary(const std::string& requestData);
	virtual void send(const std::string& requestData);
	virtual void sendTwoBinaryParts(const std::string& requestData1, const std::string& requestData2);

	virtual std::optional<std::string> receiveBinary();
	virtual std::optional<std::string> receive();

	virtual void cancel();
	virtual bool isCanceled();

	virtual bool hasTimedout();

	virtual void terminate();

private:
	void sendRequest(const std::string& requestPart1, const std::string& requestPart2);
	void sendRequest(const std::string& requestPart1, const std::string& requestPart2, const std::string& requestPart3);
	bool receiveMessage(zmq::message_t& message);

	int m_pollingTime;
	int m_timeout;
	std::unique_ptr<zmq::socket_t> m_requester;
	std::atomic_bool m_canceled;
	std::atomic_bool m_timedout;
};

}
}
}

#endif