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

#include "Application.h"
#include "Serializer.h"
#include "JSON.h"
#include "../../base/impl/RequestSocketImpl.h"
#include "../../base/impl/ServicesImpl.h"
#include "../../base/Messages.h"
#include <sstream>

namespace cameo {
namespace coms {

RequestImpl::RequestImpl(application::This * application, const std::string & requesterApplicationName, int requesterApplicationId, const std::string& message, const std::string& serverUrl, int serverPort, int requesterPort) :
	m_application(application),
	m_message(message),
	m_requesterApplicationName(requesterApplicationName),
	m_requesterApplicationId(requesterApplicationId),
	m_timeout(0) {

	std::stringstream requesterEndpoint;
	requesterEndpoint << serverUrl << ":" << requesterPort;
	m_requesterEndpoint = requesterEndpoint.str();

	std::stringstream requesterServerEndpoint;
	requesterServerEndpoint << serverUrl << ":" << serverPort;
	m_requesterServerEndpoint = requesterServerEndpoint.str();
}

RequestImpl::~RequestImpl() {
}

void RequestImpl::setTimeout(int value) {
	m_timeout = value;
}

bool RequestImpl::replyBinary(const std::string& response) {

	json::StringObject request;
	request.pushKey(message::TYPE);
	request.pushInt(message::RESPONSE);

	// Create a request socket. It is created for each request that could be optimized.
	std::unique_ptr<RequestSocketImpl> requestSocket = m_application->createRequestSocket(m_requesterEndpoint);

	try {
		requestSocket->request(request.toString(), response);
	}
	catch (const ConnectionTimeout&) {
		return false;
	}

	return true;
}

bool RequestImpl::reply(const std::string& response) {

	// Encode the data.
	std::string result;
	serialize(response, result);

	return replyBinary(result);
}

}
}


