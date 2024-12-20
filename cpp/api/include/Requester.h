/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
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
class CAMEO_EXPORT Requester : public Object, public Timeoutable, public Cancelable {

public:
	/**
	 * Destructor.
	 */
	~Requester() override;

	/**
	 * Returns a new requester.
	 * \param app The application where the responder is defined.
	 * \param responderName The responder name.
	 * \param checkApp If true, a thread is checking the state of the app and cancels the requester if it fails.
	 * \return A new Requester object.
	 */
	static std::unique_ptr<Requester> create(const App &app, const std::string &responderName);

	/**
	 * Sets the check app feature. Default value is false.
	 * \param value True if app is checked.
	 */
	void setCheckApp(bool value);

	/**
	 * Sets the timeout.
	 * \param value The value.
	 */
	void setTimeout(int value) override;

	/**
	 * Sets the polling time.
	 * \param value The value.
	 */
	void setPollingTime(int value);

	/**
	 * Initializes the requester.
	 * \throws InitException if the requester cannot be created.
	 * \throws SynchronizationTimeout if the requester cannot synchronize the responder.
	 */
	void init() override;

	/**
	 * Terminates the communication.
	 */
	void terminate() override;

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
	 * If the requester timed out in the last request, then it is reinitialized and can time out during the synchronization.
	 * \param request The request.
	 */
	void send(const std::string &request);

	/**
	 * Sends a request in two parts.
	 * If the requester timed out in the last request, then it is reinitialized and can time out during the synchronization.
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

	class Checker {

	public:
		Checker(Requester &requester);

		void start();
		void terminate();

	private:
		Requester& m_requester;
		std::unique_ptr<App> m_app;
		std::unique_ptr<std::thread> m_thread;
	};

	const App & m_app;
	std::string m_responderName;
	bool m_checkApp;
	int m_timeout;
	bool m_useProxy;
	std::string m_appName;
	int m_appId;
	Endpoint m_appEndpoint;
	std::unique_ptr<RequesterImpl> m_impl;
	std::unique_ptr<Waiting> m_waiting;
	std::string m_key;
	std::unique_ptr<App::Com::KeyValueGetter> m_keyValueGetter;
	std::unique_ptr<Checker> m_checker;
};

}
}

/**
 * Stream operator for a Requester object.
 */
CAMEO_EXPORT std::ostream& operator<<(std::ostream&, const cameo::coms::Requester&);

#endif