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

#ifndef CAMEO_REQUESTERZMQ_H_
#define CAMEO_REQUESTERZMQ_H_

#include "../RequesterImpl.h"
#include "../../../base/Waiting.h"
#include "Strings.h"
#include <string>
#include <optional>
#include <zmq.hpp>

namespace cameo {

class RequestSocket;

namespace coms {

class RequesterZmq : public RequesterImpl {

public:
	virtual ~RequesterZmq();

	void init(const Endpoint& endpoint, int responderPort);
	void sendBinary(const std::string& requestData);
	void send(const std::string& requestData);
	void sendTwoBinaryParts(const std::string& requestData1, const std::string& requestData2);

	std::optional<std::string> receiveBinary();
	std::optional<std::string> receive();

	void cancel();
	bool isCanceled();
	void terminate();

private:
	int m_requesterPort;
	std::string m_name;
	std::unique_ptr<RequestSocket> m_requestSocket;
	std::unique_ptr<zmq::socket_t> m_requester;
	bool m_canceled;
};

}
}

#endif
