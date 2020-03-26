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
#include "zmq.hpp"

namespace cameo {

namespace application {
	class This;
}

class RequestImpl {

public:
	RequestImpl(application::This * application, const std::string & requesterApplicationName, int requesterApplicationId, const std::string& message, const std::string& serverUrl, int serverPort, int requesterPort);
	~RequestImpl();

	void setTimeout(int value);

	void replyBinary(const std::string& response);
	void reply(const std::string& response);

	application::This * m_application;
	std::string m_requesterEndpoint;
	std::string m_message;
	std::string m_message2;
	std::string m_requesterApplicationName;
	int m_requesterApplicationId;
	std::string m_requesterServerEndpoint;
	int m_timeout;
};

}

#endif
