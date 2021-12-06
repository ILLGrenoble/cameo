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

#ifndef CAMEO_RESPONDERIMPL_H_
#define CAMEO_RESPONDERIMPL_H_

#include "../../base/impl/GenericWaitingImpl.h"

#include "zmq.hpp"
#include <string>
#include <vector>
#include <memory>

namespace cameo {
namespace coms {

class RequestImpl;

class ResponderImpl {

public:
	ResponderImpl(int responderPort, const std::string& name);
	~ResponderImpl();

	void cancel();
	WaitingImpl * waiting();

	std::unique_ptr<RequestImpl> receive();
	void terminate();

	int m_responderPort;
	std::string m_name;
	std::unique_ptr<zmq::socket_t> m_responder;
	bool m_canceled;

	static const std::string RESPONDER_PREFIX;
};

}
}

#endif
