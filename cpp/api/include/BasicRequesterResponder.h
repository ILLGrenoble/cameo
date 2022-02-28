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
#include "ResponderCreationException.h"
#include "RequesterCreationException.h"

namespace cameo {
namespace coms {
namespace basic {

class Responder;
class ResponderImpl;
class RequesterImpl;

///////////////////////////////////////////////////////////////////////////
// Request

class Request {

	friend class Responder;
	friend std::ostream& operator<<(std::ostream&, const Request&);

public:
	Request(const std::string & requesterApplicationName, int requesterApplicationId, const std::string& serverUrl, int serverPort, const std::string& messagePart1, const std::string& messagePart2);
	~Request();

	std::string getObjectId() const;
	std::string getRequesterEndpoint() const;

	const std::string& getBinary() const;
	std::string get() const;
	const std::string& getSecondBinaryPart() const;

	void setTimeout(int value);

	bool replyBinary(const std::string &response);
	bool reply(const std::string &response);

	application::ServerAndInstance connectToRequester(int options = 0, bool useProxy = false);

private:
	void setResponder(Responder* responder);

	Responder* m_responder;
	std::string m_messagePart1;
	std::string m_messagePart2;
	std::string m_requesterApplicationName;
	int m_requesterApplicationId;
	std::string m_requesterServerEndpoint;
	int m_timeout;
};

///////////////////////////////////////////////////////////////////////////
// Responder

class Responder {

	friend class Request;
	friend std::ostream& operator<<(std::ostream&, const Responder&);

public:
	~Responder();

	/** \brief Returns the responder with name.
	 * throws ResponderCreationException.
	 */
	static std::unique_ptr<Responder> create(const std::string &name);

	/// Returns the name of the responder
	const std::string& getName() const;

	void cancel();

	/** \brief Receive a request
	 * blocking command
	 */
	std::unique_ptr<Request> receive();

	/** check if it has been canceled */
	bool isCanceled() const;

	static const std::string KEY;
	static const std::string PORT;

private:
	Responder(const std::string &name);
	void init(const std::string &name);

	void reply(const std::string& type, const std::string& response);

	std::string m_name;
	std::unique_ptr<ResponderImpl> m_impl;
	std::unique_ptr<Waiting> m_waiting;
	std::string m_key;
};

///////////////////////////////////////////////////////////////////////////
// Requester

class Requester {

	friend std::ostream& operator<<(std::ostream&, const Requester&);

public:
	virtual ~Requester();

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
	void init(application::Instance & app, const std::string & responderName);
	void tryInit(application::Instance & app);

	bool m_useProxy;
	std::string m_responderName;
	std::string m_appName;
	int m_appId;
	Endpoint m_appEndpoint;
	std::unique_ptr<RequesterImpl> m_impl;
	std::unique_ptr<Waiting> m_waiting;
	std::string m_key;
};

std::ostream& operator<<(std::ostream&, const Request&);
std::ostream& operator<<(std::ostream&, const Responder&);
std::ostream& operator<<(std::ostream&, const Requester&);

}
}
}

#endif
