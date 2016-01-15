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

#ifndef CAMEO_REQUESTIMPL_H_
#define CAMEO_REQUESTIMPL_H_

#include <string>
#include <vector>
#include <stdint.h>
#include "../../cameo/impl/zmq.hpp"

namespace cameo {

namespace application {
	class This;
}

class RequestImpl {

public:
	RequestImpl(const application::This * application, const std::string & requesterEndpoint, const std::string& message, int requesterApplicationId);
	~RequestImpl();

	void sendBinary(const std::string& response);
	void send(const std::string& response);

	const application::This * m_application;
	std::string m_requesterEndpoint;
	std::string m_message;
	int m_requesterApplicationId;
};

}

#endif
