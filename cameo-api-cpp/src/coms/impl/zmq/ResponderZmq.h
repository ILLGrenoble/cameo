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

#ifndef CAMEO_RESPONDERZMQ_H_
#define CAMEO_RESPONDERZMQ_H_

#include "../ResponderImpl.h"
#include <zmq.hpp>

namespace cameo {
namespace coms {

class Request;

class ResponderZmq : public ResponderImpl {

public:
	ResponderZmq();
	~ResponderZmq();

	void init(int responderPort);

	void cancel();
	bool isCanceled();

	std::unique_ptr<Request> receive();

private:
	zmq::message_t * responseToRequest();
	zmq::message_t * responseToCancelResponder();
	zmq::message_t * responseToUnknownRequest();

	void terminate();

	int m_responderPort;
	std::unique_ptr<zmq::socket_t> m_responder;
	bool m_canceled;
};

}
}

#endif