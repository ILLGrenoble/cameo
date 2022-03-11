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

#ifndef CAMEO_COMS_BASIC_REQUESTERRESPONDER_H_
#define CAMEO_COMS_BASIC_REQUESTERRESPONDER_H_

#include "Application.h"
#include "RequesterCreationException.h"

namespace cameo {
namespace coms {

class RequesterImpl;

///////////////////////////////////////////////////////////////////////////
// Requester

class Requester {

	friend std::ostream& operator<<(std::ostream&, const Requester&);

public:
	~Requester();
	void terminate();

	/**
	 * Returns the responder with name.
	 * throws RequesterCreationException.
	 */
	static std::unique_ptr<Requester> create(application::Instance &app, const std::string &name);

	void setPollingTime(int value);
	void setTimeout(int value);

	const std::string& getResponderName() const;
	const std::string& getAppName() const;
	int getAppId() const;
	Endpoint getAppEndpoint() const;

	void sendBinary(const std::string &request);
	void send(const std::string &request);
	void sendTwoBinaryParts(const std::string &request1, const std::string &request2);

	/**
	 * Returns a string or nothing if the requester is canceled or a timeout occurred.
	 */
	std::optional<std::string> receiveBinary();

	/**
	 * Returns a string or nothing if the requester is canceled or a timeout occurred.
	 */
	std::optional<std::string> receive();

	void cancel();

	bool isCanceled() const;
	bool hasTimedout() const;

private:
	Requester();
	void init(const application::Instance & app, const std::string & responderName);

	bool m_useProxy;
	std::string m_responderName;
	std::string m_appName;
	int m_appId;
	Endpoint m_appEndpoint;
	std::unique_ptr<RequesterImpl> m_impl;
	std::unique_ptr<Waiting> m_waiting;
	std::string m_key;
};

std::ostream& operator<<(std::ostream&, const Requester&);

}
}

#endif
