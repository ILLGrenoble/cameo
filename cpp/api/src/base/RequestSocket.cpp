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

#include "RequestSocket.h"

#include "ConnectionTimeout.h"
#include "../factory/ImplFactory.h"
#include "impl/zmq/RequestSocketZmq.h"
#include <iostream>
#include <chrono>
#include <thread>

using namespace std;

namespace cameo {

RequestSocket::RequestSocket(Context * context, const std::string& endpoint, int timeout) {

	m_impl = ImplFactory::createRequestSocket(context, endpoint);
	m_impl->setTimeout(timeout);
}

RequestSocket::~RequestSocket() {
}

void RequestSocket::setTimeout(int timeout) {
	m_impl->setTimeout(timeout);
}

std::string RequestSocket::request(const std::string& request, int overrideTimeout) {
	return m_impl->request(request, overrideTimeout);
}

std::string RequestSocket::request(const std::string& requestPart1, const std::string& requestPart2, int overrideTimeout) {
	return m_impl->request(requestPart1, requestPart2, overrideTimeout);
}

std::string RequestSocket::request(const std::string& requestPart1, const std::string& requestPart2, const std::string& requestPart3, int overrideTimeout) {
	return m_impl->request(requestPart1, requestPart2, requestPart3, overrideTimeout);
}

json::Object RequestSocket::requestJSON(const std::string& request, int overrideTimeout) {

	std::string reply = this->request(request, overrideTimeout);

	// Get the JSON response.
	json::Object response;
	json::parse(response, reply);

	return response;
}

json::Object RequestSocket::requestJSON(const std::string& requestPart1, const std::string& requestPart2, int overrideTimeout) {

	std::string reply = this->request(requestPart1, requestPart2, overrideTimeout);

	// Get the JSON response.
	json::Object response;
	json::parse(response, reply);

	return response;
}

json::Object RequestSocket::requestJSON(const std::string& requestPart1, const std::string& requestPart2, const std::string& requestPart3, int overrideTimeout) {

	std::string reply = this->request(requestPart1, requestPart2, requestPart3, overrideTimeout);

	// Get the JSON response.
	json::Object response;
	json::parse(response, reply);

	return response;
}

}
