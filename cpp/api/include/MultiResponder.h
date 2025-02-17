/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

#ifndef CAMEO_COMS_MULTI_RESPONDER_H_
#define CAMEO_COMS_MULTI_RESPONDER_H_

#include "ServerAndApp.h"

namespace cameo {
namespace coms {

/**
 * Namespace for the multi implementation of the responder.
 */
namespace multi {

class Responder;
class ResponderImpl;
class ResponderRouter;
class ResponderRouterImpl;

///////////////////////////////////////////////////////////////////////////
// Request

/**
 * Class defining a request received by the multi responder.
 */
class CAMEO_EXPORT Request {

	friend class Responder;

public:
	/**
	 * Constructor.
	 * \param requesterApplicationName The requester application name.
	 * \param requesterApplicationId The request application id.
	 * \param serverEndpoint The server endpoint.
	 * \param serverProxyPort The server proxy port.
	 * \param messagePart1 The message part 1.
	 * \param messagePart2 The message part 2.
	 */
	Request(const std::string & requesterApplicationName, int requesterApplicationId, const std::string& serverEndpoint, int serverProxyPort, const std::string& messagePart1, const std::string& messagePart2);

	/**
	 * Destructor.
	 */
	~Request();

	/**
	 * Gets the requester endpoint.
	 * \return The requester endpoint.
	 */
	std::string getRequesterEndpoint() const;

	/**
	 * Gets the first part.
	 * \return The first part.
	 */
	const std::string& get() const;

	/**
	 * Gets the first part.
	 * \return The first part.
	 */
	const std::string& getFirstPart() const;

	/**
	 * Gets the second part.
	 * \return The second part.
	 */
	const std::string& getSecondPart() const;

	/**
	 * Replies to the request.
	 * \param response The response.
	 */
	void reply(const std::string &response);

	/**
	 * Connects to the requester application.
	 * \param options The options to the connection.
	 * \param timeout Timeout for the server initialization.
	 * \return The ServerAndApp pair.
	 */
	std::unique_ptr<ServerAndApp> connectToRequester(int options = 0, int timeout = 0);

	/**
	 * Returns a string representation of the request
	 * \return The string representation.
	 */
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
};

///////////////////////////////////////////////////////////////////////////
// ResponderRouter

/**
 * Class defining a responder router.
 * Requests are dispatched to the multi responders that process them in parallel.
 */
class CAMEO_EXPORT ResponderRouter : public Object, public Cancelable {

	friend class Responder;
	friend class Request;

public:
	/**
	 * Destructor.
	 */
	~ResponderRouter() override;

	/**
	 * Returns the responder router with name.
	 * \param name The name.
	 * \return The new ResponderRouter object.
	 */
	static std::unique_ptr<ResponderRouter> create(const std::string &name);

	/**
	 * Initializes the responder router.
	 * \throws InitException if the responder cannot be initialized.
	 */
	void init() override;

	/**
	 * Terminates the communication.
	 */
	void terminate() override;

	/**
	 * Cancels the responder router running in another thread.
	 */
	void cancel() override;

	/**
	 * Returns true if the responder router has been canceled.
	 * \return True if canceled.
	 */
	bool isCanceled() const override;

	/**
	 * Sets the polling time.
	 * \param value The value.
	 */
	void setPollingTime(int value);

	/**
	 * Returns the name of the responder.
	 * \return The name.
	 */
	const std::string& getName() const;

	/**
	 * Runs the responder router. This is a blocking call.
	 */
	void run();

	/**
	 * Returns a string representation of the responder router.
	 * \return The string representation.
	 */
	std::string toString() const override;

	/**
	 * Constant uuid for the unique responder key.
	 */
	static const std::string KEY;

	/**
	 * Constant uuid for the unique responder port.
	 */
	static const std::string PORT;

private:
	ResponderRouter(const std::string &name);

	const std::string& getDealerEndpoint() const;

	std::string m_name;
	std::unique_ptr<ResponderRouterImpl> m_impl;
	std::unique_ptr<Waiting> m_waiting;
	std::string m_key;
	std::string m_dealerEndpoint;
};

///////////////////////////////////////////////////////////////////////////
// Responder

/**
 * Class defining a responder for the responder router.
 * Requests are processed sequentially.
 */
class CAMEO_EXPORT Responder : public Object, public Cancelable {

	friend class Request;

public:
	/**
	 * Destructor.
	 */
	~Responder() override;

	/**
	 * Creates a new responder.
	 * \param router The router.
	 * \return A new Responder object.
	 */
	static std::unique_ptr<Responder> create(const ResponderRouter& router);

	/**
	 * Initializes the responder.
	 */
	void init() override;

	/**
	 * Terminates the communication.
	 */
	void terminate() override;

	/**
	 * Cancels the responder waiting in another thread.
	 */
	void cancel() override;

	/**
	 * Returns true if it has been canceled.
	 * \return True if canceled.
	 */
	bool isCanceled() const override;

	/**
	 * Receives a request. This is a blocking command until a Request is received.
	 * \return A Request object.
	 */
	std::unique_ptr<Request> receive();

	/**
	 * Returns a string representation of the responder.
	 * \return The string representation.
	 */
	std::string toString() const override;

private:
	Responder(const std::string& dealerEndpoint);

	void reply(const std::string& type, const std::string& response);

	std::string m_dealerEndpoint;
	std::unique_ptr<ResponderImpl> m_impl;
	std::unique_ptr<Waiting> m_waiting;
};

}
}
}

/**
 * Stream operator for a Request object.
 */
CAMEO_EXPORT std::ostream& operator<<(std::ostream&, const cameo::coms::multi::Request&);

/**
 * Stream operator for a ResponderRouter object.
 */
CAMEO_EXPORT std::ostream& operator<<(std::ostream&, const cameo::coms::multi::ResponderRouter&);

/**
 * Stream operator for a Responder object.
 */
CAMEO_EXPORT std::ostream& operator<<(std::ostream&, const cameo::coms::multi::Responder&);

#endif