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

#ifndef CAMEO_REQUESTSOCKET_H_
#define CAMEO_REQUESTSOCKET_H_

#include "JSON.h"
#include <string>
#include <memory>

namespace cameo {

class Context;
class RequestSocketImpl;

/**
 * Class defining a request socket.
 */
class RequestSocket {

public:
	/**
	 * Constructor.
	 * \param context The context.
	 * \param endpoint The endpoint.
	 * \param responderIdentity The responder identity.
	 * \param timeout The timeout.
	 */
	RequestSocket(Context * context, const std::string& endpoint, const std::string& responderIdentity, int timeout = 0);

	/**
	 * Destructor.
	 */
	virtual ~RequestSocket();

	/**
	 * Sets the timeout.
	 * \param timeout The timeout.
	 */
	void setTimeout(int timeout);

	/**
	 * Send a request.
	 * \param request The request.
	 * \param overrideTimeout Timeout that overrides the timeout defined previously.
	 * \return The response.
	 */
	std::string request(const std::string& request, int overrideTimeout = -1);

	/**
	 * Send a request.
	 * \param requestPart1 The request part 1.
	 * \param requestPart2 The request part 2.
	 * \param overrideTimeout Timeout that overrides the timeout defined previously.
	 * \return The response.
	 */
	std::string request(const std::string& requestPart1, const std::string& requestPart2, int overrideTimeout = -1);

	/**
	 * Send a request.
	 * \param requestPart1 The request part 1.
	 * \param requestPart2 The request part 2.
	 * \param requestPart3 The request part 3.
	 * \param overrideTimeout Timeout that overrides the timeout defined previously.
	 * \return The response.
	 */
	std::string request(const std::string& requestPart1, const std::string& requestPart2, const std::string& requestPart3, int overrideTimeout = -1);

	/**
	 * Send a request.
	 * \param request The JSON string request.
	 * \param overrideTimeout Timeout that overrides the timeout defined previously.
	 * \return The JSON object response.
	 */
	json::Object requestJSON(const std::string& request, int overrideTimeout = -1);

	/**
	 * Send a request.
	 * \param requestPart1 The JSON string request part 1.
	 * \param requestPart2 The request part 2.
	 * \param overrideTimeout Timeout that overrides the timeout defined previously.
	 * \return The JSON object response.
	 */
	json::Object requestJSON(const std::string& requestPart1, const std::string& requestPart2, int overrideTimeout = -1);

	/**
	 * Send a request.
	 * \param requestPart1 The JSON string request part 1.
	 * \param requestPart2 The request part 2.
	 * \param requestPart3 The request part 3.
	 * \param overrideTimeout Timeout that overrides the timeout defined previously.
	 * \return The JSON object response.
	 */
	json::Object requestJSON(const std::string& requestPart1, const std::string& requestPart2, const std::string& requestPart3, int overrideTimeout = -1);

private:
	std::unique_ptr<RequestSocketImpl> m_impl;
};

}

#endif
