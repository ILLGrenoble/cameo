/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

#ifndef CAMEO_PUBLISHERSUBSCRIBER_H_
#define CAMEO_PUBLISHERSUBSCRIBER_H_

#include "Application.h"

namespace cameo {
namespace coms {

class PublisherImpl;
class SubscriberImpl;
class Requester;

namespace basic {

class Responder;

}

///////////////////////////////////////////////////////////////////////////
// Publisher

/**
 * Class defining a publisher. It can be synchronized with a certain number of subscribers or not.
 */
class CAMEO_EXPORT Publisher : public Object, public Cancelable {

public:
	/**
	 * Destructor.
	 */
	~Publisher() override;

	/**
	 * Returns the publisher with name.
	 * \param name The name.
	 * \return A new Publisher object.
	 */
	static std::unique_ptr<Publisher> create(const std::string &name);

	/**
	 * Sets the subscribers synchronized. By default, the subscribers are not synchronized.
	 * \param value True if synchronized.
	 */
	void setSyncSubscribers(bool value);

	/**
	 * Sets the wait for subscribers. If set then the subscribers are set synchronized.
	 * \param numberOfSubscribers The number of subscribers.
	 */
	void setWaitForSubscribers(int numberOfSubscribers);

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

	/**
	 * Constant uuid for the unique publisher key.
	 */
	static const std::string KEY;

	/**
	 * Port key for the JSON object stored for the responder key.
	 */
	static const std::string PUBLISHER_PORT;

	/**
	 * Number of subscribers key for the JSON object stored for the responder key.
	 */
	static const std::string NUMBER_OF_SUBSCRIBERS;

	/**
	 * Sync subscribers key for the JSON object stored for the responder key.
	 */
	static const std::string SYNC_SUBSCRIBERS;

	/**
	 * Prefix for the temporary responder used for the synchronization.
	 */
	static const std::string RESPONDER_PREFIX;

	/**
	 * Message type for the responder used for the synchronization.
	 */
	static const int SUBSCRIBE_PUBLISHER = 100;

private:
	static const int CANCEL_RESPONDER = 0;

	Publisher(const std::string &name);

	void responderLoop();
	bool waitForSubscribers();

	std::string m_name;
	bool m_syncSubscribers = false;
	int m_numberOfSubscribers = 0;
	std::unique_ptr<PublisherImpl> m_impl;
	std::unique_ptr<Waiting> m_waiting;
	std::string m_key;
	std::unique_ptr<basic::Responder> m_responder;
	std::unique_ptr<std::thread> m_responderThread;
	ConcurrentQueue<int> m_responderQueue;
	std::atomic_bool m_canceled;
};

///////////////////////////////////////////////////////////////////////////
// Subscriber

/**
 * Class defining a subscriber.
 */
class CAMEO_EXPORT Subscriber : public Object, public Timeoutable, public Cancelable {

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
	 * \param checkApp If true, a thread is checking the state of the app and cancels the subscriber if it fails.
	 * \return A new Subscriber object.
	 */
	static std::unique_ptr<Subscriber> create(const App & app, const std::string &publisherName);

	/**
	 * Sets the check app feature. Default value is false.
	 * \param value True if app is checked.
	 */
	void setCheckApp(bool value);

	/**
	 * Initializes the subscriber.
	 * \throws InitException if the subscriber cannot be created.
	 * \throws SynchronizationTimeout if the subscriber cannot synchronize the publisher.
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
	 * Sets the polling time.
	 * \param value The value.
	 */
	void setPollingTime(int value);

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
	 * Returns true if the subscriber has timed out.
	 * \return True if the subscriber has timed out.
	 */
	bool hasTimedout() const;

	/**
	 * Returns a string representation of the subscriber.
	 * \return The string representation.
	 */
	std::string toString() const override;

private:
	Subscriber(const App & app, const std::string &publisherName);
	void synchronize(const TimeoutCounter& timeout, int numberOfSubscribers, bool syncSubscribers);

	const App & m_app;
	std::string m_publisherName;
	bool m_checkApp = false;
	int m_timeout;
	bool m_useProxy;
	std::string m_appName;
	int m_appId;
	Endpoint m_appEndpoint;
	std::unique_ptr<SubscriberImpl> m_impl;
	std::unique_ptr<Waiting> m_waiting;
	std::string m_key;
	std::unique_ptr<App::Com::KeyValueGetter> m_keyValueGetter;
	std::unique_ptr<Requester> m_requester;
};

}
}

/**
 * Stream operator for a Publisher object.
 */
CAMEO_EXPORT std::ostream& operator<<(std::ostream&, const cameo::coms::Publisher&);

/**
 * Stream operator for a Subscriber object.
 */
CAMEO_EXPORT std::ostream& operator<<(std::ostream&, const cameo::coms::Subscriber&);

#endif