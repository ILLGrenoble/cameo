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
#include "EventStreamSocket.h"

namespace cameo {

class ServicesImpl;

class Services {

public:
	Services();
	~Services();

	void setImpl(ServicesImpl * impl);
	std::vector<std::string> split(const std::string& info);

	void setTimeout(int timeout);
	int getTimeout() const;
	const std::string& getEndpoint() const;
	const std::string& getUrl() const;
	int getPort() const;
	const std::string& getStatusEndpoint() const;

	bool isAvailable(int timeout) const;
	void initStatus();
	std::auto_ptr<EventStreamSocket> openEventStream();

	std::string m_serverEndpoint;
	std::string m_url;
	int m_port;
	int m_statusPort;
	std::string m_serverStatusEndpoint;
	ServicesImpl * m_impl;
};

}

#endif
