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

#ifndef CAMEO_SERVICES_H_
#define CAMEO_SERVICES_H_

#include <string>
#include <vector>
#include "Strings.h"
#include "EventStreamSocket.h"
#include "OutputStreamSocket.h"

namespace cameo {

class ServicesImpl;
class RequestSocketImpl;

class Services {

public:
	Services();
	~Services();

	void terminate();

	void init();
	void initRequestSocket();
	std::vector<std::string> split(const std::string& info);

	void setTimeout(int timeout);
	int getTimeout() const;
	const Endpoint& getEndpoint() const;
	const std::string& getUrl() const;
	std::array<int, 3> getVersion() const;
	const std::string& getStatusEndpoint() const;

	bool isAvailable(int timeout) const;
	void retrieveServerVersion();
	void initStatus();
	std::unique_ptr<EventStreamSocket> openEventStream();
	int getStreamPort(const std::string& name);
	std::unique_ptr<OutputStreamSocket> createOutputStreamSocket(const std::string& name);
	std::unique_ptr<RequestSocketImpl> createRequestSocket(const std::string& endpoint);
	std::unique_ptr<RequestSocketImpl> createRequestSocket(const std::string& endpoint, int timeout);

	Endpoint m_serverEndpoint;
	std::array<int, 3> m_serverVersion;
	std::string m_url;
	int m_statusPort;
	std::string m_serverStatusEndpoint;
	std::unique_ptr<ServicesImpl> m_impl;
	std::unique_ptr<RequestSocketImpl> m_requestSocket;
};

}

#endif
