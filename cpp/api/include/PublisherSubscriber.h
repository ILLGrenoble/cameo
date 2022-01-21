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
#include "PublisherCreationException.h"
#include "SubscriberCreationException.h"

namespace cameo {
namespace coms {

class PublisherImpl;
class SubscriberImpl;

///////////////////////////////////////////////////////////////////////////
// Publisher

class Publisher {

	friend class cameo::application::This;

public:
	~Publisher();

	/**
	 * Returns the publisher with name.
	 * throws PublisherCreationException.
	 */
	static std::unique_ptr<Publisher> create(const std::string &name, int numberOfSubscribers = 0);

	const std::string& getName() const;

	/**
	 * Returns true if the wait succeeds or false if it was canceled.
	 */
	bool waitForSubscribers() const;
	void cancelWaitForSubscribers();

	void sendBinary(const std::string &data) const;
	void send(const std::string &data) const;
	void sendTwoBinaryParts(const std::string &data1, const std::string &data2) const;
	void sendEnd() const;

	/**
	 * Deprecated.
	 * TODO remove in next version.
	 */
	bool hasEnded() const;

	bool isEnded() const;

	static const std::string KEY;
	static const std::string PUBLISHER_PORT;
	static const std::string SYNCHRONIZER_PORT;
	static const std::string NUMBER_OF_SUBSCRIBERS;

private:
	Publisher(const std::string &name, int numberOfSubscribers);
	void init(const std::string &name);

	std::string m_name;
	int m_numberOfSubscribers;
	std::unique_ptr<PublisherImpl> m_impl;
	std::unique_ptr<Waiting> m_waiting;
	std::string m_key;
};

///////////////////////////////////////////////////////////////////////////
// Subscriber

class Subscriber {

	friend class cameo::Server;
	friend class cameo::application::Instance;

public:
	~Subscriber();

	static std::unique_ptr<Subscriber> create(application::Instance & app, const std::string &publisherName);

	const std::string& getPublisherName() const;
	const std::string& getAppName() const;
	int getAppId() const;
	Endpoint getAppEndpoint() const;

	/**
	 * Deprecated.
	 * TODO remove in next version.
	 */
	bool hasEnded() const;

	bool isEnded() const;
	bool isCanceled() const;

	/**
	 * Returns a string or nothing if the stream has finished.
	 */
	std::optional<std::string> receiveBinary() const;

	/**
	 * Returns a string or nothing if the stream has finished.
	 */
	std::optional<std::string> receive() const;

	/**
	 * Returns a tuple of strings or nothing if the stream has finished.
	 */
	std::optional<std::tuple<std::string, std::string>> receiveTwoBinaryParts() const;

	void cancel();

private:
	Subscriber();
	void init(application::Instance &app, const std::string &publisherName);
	static std::unique_ptr<Subscriber> createSubscriber(application::Instance & app, const std::string &publisherName);

	std::string m_publisherName;
	std::string m_appName;
	int m_appId;
	Endpoint m_appEndpoint;
	std::unique_ptr<SubscriberImpl> m_impl;
	std::unique_ptr<Waiting> m_waiting;
};

std::ostream& operator<<(std::ostream&, const cameo::coms::Publisher&);
std::ostream& operator<<(std::ostream&, const cameo::coms::Subscriber&);

}
}

#endif
