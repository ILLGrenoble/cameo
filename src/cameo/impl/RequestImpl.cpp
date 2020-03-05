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

#include "RequestImpl.h"

#include "../Application.h"
#include "../Serializer.h"
#include "ServicesImpl.h"
#include <sstream>

using namespace std;

namespace cameo {

RequestImpl::RequestImpl(const application::This * application, const std::string & requesterApplicationName, int requesterApplicationId, const std::string& message, const std::string& serverUrl, int serverPort, int requesterPort) :
	m_application(application),
	m_message(message),
	m_requesterApplicationName(requesterApplicationName),
	m_requesterApplicationId(requesterApplicationId) {

	stringstream requesterEndpoint;
	requesterEndpoint << serverUrl << ":" << requesterPort;
	m_requesterEndpoint = requesterEndpoint.str();

	stringstream requesterServerEndpoint;
	requesterServerEndpoint << serverUrl << ":" << serverPort;
	m_requesterServerEndpoint = requesterServerEndpoint.str();
}

RequestImpl::~RequestImpl() {
}

void RequestImpl::replyBinary(const std::string& response) {
	string strRequestType = m_application->m_impl->createRequestType(PROTO_RESPONSE);
	m_application->m_impl->tryRequestWithOnePartReply(strRequestType, response, m_requesterEndpoint);
}

void RequestImpl::reply(const std::string& response) {

	// encode the data
	string result;
	serialize(response, result);

	replyBinary(result);
}

}

