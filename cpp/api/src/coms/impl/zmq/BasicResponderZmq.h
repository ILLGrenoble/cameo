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

#ifndef CAMEO_COMS_BASIC_RESPONDERZMQ_H_
#define CAMEO_COMS_BASIC_RESPONDERZMQ_H_

#include <zmq.hpp>
#include "../BasicResponderImpl.h"

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
	zmq::message_t * responseToRequest();
	zmq::message_t * responseToCancelResponder();

	void terminate();

	int m_responderPort;
	std::unique_ptr<zmq::socket_t> m_responder;
	std::string m_responderIdentity;

	std::unique_ptr<zmq::message_t> m_proxyIdentity;
	std::unique_ptr<zmq::message_t> m_requesterIdentity;

	bool m_canceled;
};

}
}
}

#endif
