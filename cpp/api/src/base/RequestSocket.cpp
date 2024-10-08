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

#include "ImplFactory.h"
#include "ConnectionTimeout.h"
#include "impl/zmq/RequestSocketZmq.h"
#include <iostream>
#include <chrono>
#include <thread>

using namespace std;

namespace cameo {

RequestSocket::RequestSocket(Context * context, const std::string& endpoint, const std::string& responderIdentity, int timeout) {

	m_impl = ImplFactory::createRequestSocket(context, endpoint, responderIdentity);
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

}
