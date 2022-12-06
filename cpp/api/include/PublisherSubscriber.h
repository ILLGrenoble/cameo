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

#ifndef CAMEO_PUBLISHERSUBSCRIBER_H_
#define CAMEO_PUBLISHERSUBSCRIBER_H_

#include "Application.h"

namespace cameo {
namespace coms {

class PublisherImpl;
class SubscriberImpl;

namespace basic {

class Responder;

}

///////////////////////////////////////////////////////////////////////////
// Publisher

/**
 * Class defining a publisher. It can be synchronized with a certain number of subscribers or not.
 */
class Publisher : public Object, public Cancelable {

	friend class cameo::This;

public:
	/**
	 * Destructor.
	 */
	~Publisher() override;

	/**
	 * Returns the publisher with name.
	 * \param name The name.
	 * \param numberOfSubscribers The number of subscribers.
	 * \return A new Publisher object.
	 */
	static std::unique_ptr<Publisher> create(const std::string &name, int numberOfSubscribers = 0);

	/**
	 * Initializes the publisher.
	 * Waits for the subscribers if their number is greater than 0. In that case init() is a blocking call.
	 */
	void init() override;

	/**
	 * Terminates the communication.
	 */
	void terminate() override;

	/**
	 * Cancels the init() call in another thread.
	 */
	void cancel() override;

	/**
	 * Returns true if is canceled.
	 * \return True if is canceled.
	 */
	bool isCanceled() const override;

	/**
	 * Gets the name.
	 * \return The name.
	 */
	const std::string& getName() const;

	/**
	 * Sends a message in one part.
	 * \param data The data to send.
	 */
	void send(const std::string &data) const;

	/**
	 * Sends a message in two parts.
	 * \param data1 The first part.
	 * \param data2 The second part.
	 */
	void sendTwoParts(const std::string &data1, const std::string &data2) const;

	/**
	 * Sends the end of the stream.
	 */
	void sendEnd() const;

	/**
	 * Returns true if the stream ended.
	 * \return True if the stream ended.
	 */
	bool hasEnded() const;

	/**
	 * Returns a string representation of the publisher.
	 * \return The string representation.
	 */
	std::string toString() const override;

	static const std::string KEY;
	static const std::string PUBLISHER_PORT;
	static const std::string NUMBER_OF_SUBSCRIBERS;
	static const std::string RESPONDER_PREFIX;
	static const int SUBSCRIBE_PUBLISHER = 100;

private:
	Publisher(const std::string &name, int numberOfSubscribers);

	bool waitForSubscribers();

	std::string m_name;
	int m_numberOfSubscribers;
	std::unique_ptr<PublisherImpl> m_impl;
	std::unique_ptr<Waiting> m_waiting;
	std::string m_key;
	std::unique_ptr<basic::Responder> m_responder;
	bool m_canceled;
};

///////////////////////////////////////////////////////////////////////////
// Subscriber

/**
 * Class defining a subscriber.
 */
class Subscriber : public Object, public Timeoutable, public Cancelable {

	friend class cameo::Server;
	friend class cameo::App;

public:
	/**
	 * Destructor.
	 */
	~Subscriber() override;

	/**
	 * Returns a new subscriber.
	 * \param app The application where the publisher is defined.
	 * \param publisherName The name of the publisher.
	 * \return A new Subscriber object.
	 */
	static std::unique_ptr<Subscriber> create(const App & app, const std::string &publisherName);

	/**
	 * Initializes the subscriber.
	 * \throws InitException if the subscriber cannot be created.
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
	 * Cancels the subscriber. Unblocks the receive() call in another thread.
	 */
	void cancel() override;

	/**
	 * Returns true if the subscriber has been canceled.
	 * \return True if the subscriber has been canceled.
	 */
	bool isCanceled() const override;

	/**
	 * Gets the publisher name.
	 * \return The publisher name.
	 */
	const std::string& getPublisherName() const;

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
	 * Returns true if the stream ended.
	 * \return True if the stream ended.
	 */
	bool hasEnded() const;

	/**
	 * Returns a string or nothing if the stream has finished.
	 * \return The string data or null.
	 */
	std::optional<std::string> receive() const;

	/**
	 * Returns a tuple of strings or nothing if the stream has finished.
	 * \return The tuple of string data or null.
	 */
	std::optional<std::tuple<std::string, std::string>> receiveTwoParts() const;

	/**
	 * Returns a string representation of the subscriber.
	 * \return The string representation.
	 */
	std::string toString() const override;

private:
	Subscriber(const App & app, const std::string &publisherName);
	void synchronize(const App & app, const TimeoutCounter& timeout);

	const App & m_app;
	std::string m_publisherName;
	int m_timeout;
	bool m_useProxy;
	std::string m_appName;
	int m_appId;
	Endpoint m_appEndpoint;
	std::unique_ptr<SubscriberImpl> m_impl;
	std::unique_ptr<Waiting> m_waiting;
	std::string m_key;
	std::unique_ptr<App::Com::KeyValueGetter> m_keyValueGetter;
};

/**
 * Stream operator for a Publisher object.
 */
std::ostream& operator<<(std::ostream&, const cameo::coms::Publisher&);

/**
 * Stream operator for a Subscriber object.
 */
std::ostream& operator<<(std::ostream&, const cameo::coms::Subscriber&);

}
}

#endif
