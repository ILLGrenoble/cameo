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

#ifndef CAMEO_COMS_LEGACY_REQUESTERRESPONDER_H_
#define CAMEO_COMS_LEGACY_REQUESTERRESPONDER_H_

#include "Application.h"
#include "ResponderCreationException.h"
#include "RequesterCreationException.h"

namespace cameo {
namespace coms {
namespace legacy {

class Responder;
class ResponderImpl;
class RequesterImpl;

///////////////////////////////////////////////////////////////////////////
// Request

class Request {

	friend class Responder;
	friend std::ostream& operator<<(std::ostream&, const Request&);

public:
	Request(const std::string & requesterApplicationName, int requesterApplicationId, const std::string& serverUrl, int serverPort, int requesterPort, const std::string& messagePart1, const std::string& messagePart2);
	~Request();

	std::string getObjectId() const;
	std::string getRequesterEndpoint() const;

	const std::string& getBinary() const;
	std::string get() const;
	const std::string& getSecondBinaryPart() const;

	void setTimeout(int value);

	bool replyBinary(const std::string &response);
	bool reply(const std::string &response);

	std::unique_ptr<application::Instance> connectToRequester();

	/**
	 * Transfers the ownership of the requester server.
	 */
	std::unique_ptr<Server> getServer();

private:
	std::string m_requesterEndpoint;
	std::string m_messagePart1;
	std::string m_messagePart2;
	std::string m_requesterApplicationName;
	int m_requesterApplicationId;
	std::string m_requesterServerEndpoint;
	int m_timeout;

	std::unique_ptr<Server> m_requesterServer;
};

///////////////////////////////////////////////////////////////////////////
// Responder

class Responder {

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
	~Requester();

	/**
	 * Returns the responder with name.
	 * throws RequesterCreationException.
	 */
	static std::unique_ptr<Requester> create(application::Instance &app, const std::string &name);

	const std::string& getResponderName() const;
	const std::string& getAppName() const;
	int getAppId() const;
	Endpoint getAppEndpoint() const;

	void sendBinary(const std::string &request);
	void send(const std::string &request);
	void sendTwoBinaryParts(const std::string &request1, const std::string &request2);

	/**
	 * Returns a string or nothing if the requester is canceled.
	 */
	std::optional<std::string> receiveBinary();

	/**
	 * Returns a string or nothing if the requester is canceled.
	 */
	std::optional<std::string> receive();

	void cancel();

	bool isCanceled() const;

private:
	Requester();
	void init(application::Instance & app, const std::string & responderName);
	void tryInit(application::Instance & app);

	std::string m_responderName;
	int m_requesterId;
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