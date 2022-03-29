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

#ifndef CAMEO_COMS_MULTI_RESPONDER_H_
#define CAMEO_COMS_MULTI_RESPONDER_H_

#include "Application.h"
#include "ResponderCreationException.h"

namespace cameo {
namespace coms {
namespace multi {

class Responder;
class ResponderImpl;
class ResponderRouter;
class ResponderRouterImpl;

///////////////////////////////////////////////////////////////////////////
// Request

class Request {

	friend class Responder;
	friend std::ostream& operator<<(std::ostream&, const Request&);

public:
	Request(const std::string & requesterApplicationName, int requesterApplicationId, const std::string& serverEndpoint, int serverProxyPort, const std::string& messagePart1, const std::string& messagePart2);
	~Request();

	std::string getObjectId() const;
	std::string getRequesterEndpoint() const;

	const std::string& getBinary() const;
	std::string get() const;
	const std::string& getSecondBinaryPart() const;

	void setTimeout(int value);

	bool replyBinary(const std::string &response);
	bool reply(const std::string &response);

	ServerAndInstance connectToRequester(int options = 0, bool useProxy = false);

	std::string toString() const;

private:
	void setResponder(Responder* responder);

	Responder* m_responder;
	std::string m_messagePart1;
	std::string m_messagePart2;
	std::string m_requesterApplicationName;
	int m_requesterApplicationId;
	Endpoint m_requesterServerEndpoint;
	int m_requesterServerProxyPort;
	int m_timeout;
};

///////////////////////////////////////////////////////////////////////////
// ResponderRouter

class ResponderRouter {

	friend class Responder;
	friend class Request;
	friend std::ostream& operator<<(std::ostream&, const ResponderRouter&);

public:
	~ResponderRouter();
	void terminate();

	/** \brief Returns the responder with name.
	 * throws ResponderCreationException.
	 */
	static std::unique_ptr<ResponderRouter> create(const std::string &name);

	void setPollingTime(int value);

	/// Returns the name of the responder
	const std::string& getName() const;

	void cancel();

	void run();

	/** check if it has been canceled */
	bool isCanceled() const;

	std::string toString() const;

	static const std::string KEY;
	static const std::string PORT;

private:
	ResponderRouter(const std::string &name);
	void init(const std::string &name);

	const std::string& getDealerEndpoint() const;

	std::string m_name;
	std::unique_ptr<ResponderRouterImpl> m_impl;
	std::unique_ptr<Waiting> m_waiting;
	std::string m_key;
	std::string m_dealerEndpoint;
};

///////////////////////////////////////////////////////////////////////////
// Responder

class Responder {

	friend class Request;
	friend std::ostream& operator<<(std::ostream&, const Responder&);

public:
	~Responder();
	void terminate();

	/** \brief Returns the responder with name.
	 * throws ResponderCreationException.
	 */
	static std::unique_ptr<Responder> create(const ResponderRouter& router);

	void cancel();

	/** \brief Receive a request
	 * blocking command
	 */
	std::unique_ptr<Request> receive();

	/** check if it has been canceled */
	bool isCanceled() const;

	std::string toString() const;

private:
	Responder();
	void init(const std::string &endpoint);

	void reply(const std::string& type, const std::string& response);

	std::unique_ptr<ResponderImpl> m_impl;
	std::unique_ptr<Waiting> m_waiting;
};

std::ostream& operator<<(std::ostream&, const Request&);
std::ostream& operator<<(std::ostream&, const ResponderRouter&);
std::ostream& operator<<(std::ostream&, const Responder&);

}
}
}

#endif
