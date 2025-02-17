/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

#ifndef CAMEO_COMS_BASIC_RESPONDER_H_
#define CAMEO_COMS_BASIC_RESPONDER_H_

#include "ServerAndApp.h"

namespace cameo {

/**
 * Namespace for the communication objects.
 */
namespace coms {

/**
 * Namespace for the basic implementation of the responder.
 */
namespace basic {

class Responder;
class ResponderImpl;

///////////////////////////////////////////////////////////////////////////
// Request

/**
 * Request received by the basic responder.
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
	Request(const std::string& requesterApplicationName, int requesterApplicationId, const std::string& serverEndpoint, int serverProxyPort, const std::string& messagePart1, const std::string& messagePart2);

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
	 * \param timeout Timeout for the server used for the initialization and subsequent requests.
	 * \return The ServerAndApp pair.
	 */
	std::unique_ptr<ServerAndApp> connectToRequester(int options = 0, int timeout = 0);

	/**
	 * Returns a string representation of the request.
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
// Responder

/**
 * Class defining a basic responder. Requests are processed sequentially.
 */
class CAMEO_EXPORT Responder : public Object, public Cancelable {

	friend class Request;

public:
	/**
	 * Destructor.
	 */
	~Responder() override;

	/**
	 * Returns the responder with name.
	 * \param name The name.
	 * \return A new Responder object.
	 */
	static std::unique_ptr<Responder> create(const std::string &name);

	/**
	 * Initializes the responder.
	 * \throws InitException if the responder cannot be initialized.
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
	 * Returns the name.
	 * \return The name.
	 */
	const std::string& getName() const;

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

	/**
	 * Constant uuid for the unique responder key.
	 */
	static const std::string KEY;

	/**
	 * Port key for the JSON object stored for the responder key.
	 */
	static const std::string PORT;

private:
	Responder(const std::string &name);

	void reply(const std::string& type, const std::string& response);

	std::string m_name;
	std::unique_ptr<ResponderImpl> m_impl;
	std::unique_ptr<Waiting> m_waiting;
	std::string m_key;
};

}
}
}

/**
 * Stream operator for a Request object.
 */
CAMEO_EXPORT std::ostream& operator<<(std::ostream&, const cameo::coms::basic::Request&);

/**
 * Stream operator for a Responder object.
 */
CAMEO_EXPORT std::ostream& operator<<(std::ostream&, const cameo::coms::basic::Responder&);

#endif