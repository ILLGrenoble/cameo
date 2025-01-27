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

#ifndef CAMEO_COMS_REQUESTERRESPONDER_H_
#define CAMEO_COMS_REQUESTERRESPONDER_H_

#include "Application.h"

namespace cameo {
namespace coms {

class RequesterImpl;

///////////////////////////////////////////////////////////////////////////
// Requester

/**
 * Class defining a requester. The request and response must be sent and received sequentially.
 */
class Requester : public Object, public Timeoutable, public Cancelable {

	friend std::ostream& operator<<(std::ostream&, const Requester&);

public:
	/**
	 * Destructor.
	 */
	~Requester() override;

	/**
	 * Returns a new requester.
	 * \param app The application where the responder is defined.
	 * \param responderName The responder name.
	 * \return A new Requester object.
	 */
	static std::unique_ptr<Requester> create(const App &app, const std::string &responderName);

	/**
	 * Initializes the requester.
	 * \throws InitException if the requester cannot be created.
	 */
	void init() override;

	/**
	 * Terminates the communication.
	 */
	void terminate() override;

	/**
	 * Sets the timeout.
	 * \param value The value.
	 */
	void setTimeout(int value) override;

	/**
	 * Gets the timeout.
	 * \return The timeout.
	 */
	int getTimeout() const override;

	/**
	 * Cancels the requester. Unblocks the receive() call in another thread.
	 */
	void cancel() override;

	/**
	 * Returns true if the requester has been canceled.
	 * \return True if the requester has been canceled.
	 */
	bool isCanceled() const override;

	/**
	 * Sets the polling time.
	 * \param value The value.
	 */
	void setPollingTime(int value);

	/**
	 * Gets the responder name.
	 * \return The responder name.
	 */
	const std::string& getResponderName() const;

	/**
	 * Gets the application name.
	 * \return The application name.
	 */
	const std::string& getAppName() const;

	/**
	 * Gets the application id.
	 * \return The application id.
	 */
	int getAppId() const;

	/**
	 * Gets the application endpoint.
	 * \return The application endpoint.
	 */
	Endpoint getAppEndpoint() const;

	/**
	 * Sends a request in one part.
	 * \param request The request.
	 */
	void send(const std::string &request);

	/**
	 * Sends a request in two parts.
	 * \param request1 The first part of the request.
	 * \param request2 The seconds part of the request.
	 */
	void sendTwoParts(const std::string &request1, const std::string &request2);

	/**
	 * Returns a string or nothing if the requester is canceled or a timeout occurred.
	 * \return The response or null.
	 */
	std::optional<std::string> receive();

	/**
	 * Returns true if the requester has timed out.
	 * \return True if the requester has timed out.
	 */
	bool hasTimedout() const;

	/**
	 * Returns a string representation of the requester.
	 * \return The string representation.
	 */
	std::string toString() const override;

private:
	Requester(const App & app, const std::string & responderName);

	const App & m_app;
	std::string m_responderName;
	int m_timeout;
	bool m_useProxy;
	std::string m_appName;
	int m_appId;
	Endpoint m_appEndpoint;
	std::unique_ptr<RequesterImpl> m_impl;
	std::unique_ptr<Waiting> m_waiting;
	std::string m_key;
	std::unique_ptr<App::Com::KeyValueGetter> m_keyValueGetter;
};

/**
 * Stream operator for a Requester object.
 */
std::ostream& operator<<(std::ostream&, const Requester&);

}
}

#endif
