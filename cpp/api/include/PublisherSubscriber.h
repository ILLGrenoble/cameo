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

class PublisherCreationException : public RemoteException {

public:
	PublisherCreationException(const std::string& message);
};


class Publisher {

	friend class cameo::This;

public:
	~Publisher();
	void terminate();

	/**
	 * Returns the publisher with name.
	 * throws PublisherCreationException.
	 */
	static std::unique_ptr<Publisher> create(const std::string &name, int numberOfSubscribers = 0);

	const std::string& getName() const;

	/**
	 * Returns true if the wait succeeds or false if it was canceled.
	 */
	bool waitForSubscribers();
	void cancelWaitForSubscribers();

	void send(const std::string &data) const;
	void sendTwoParts(const std::string &data1, const std::string &data2) const;
	void sendEnd() const;

	/**
	 * Deprecated.
	 * TODO remove in next version.
	 */
	bool hasEnded() const;

	bool isEnded() const;

	std::string toString() const;

	static const std::string KEY;
	static const std::string PUBLISHER_PORT;
	static const std::string NUMBER_OF_SUBSCRIBERS;
	static const std::string RESPONDER_PREFIX;
	static const int SUBSCRIBE_PUBLISHER = 100;

private:
	Publisher(const std::string &name, int numberOfSubscribers);
	void init(const std::string &name);

	std::string m_name;
	int m_numberOfSubscribers;
	std::unique_ptr<PublisherImpl> m_impl;
	std::unique_ptr<Waiting> m_waiting;
	std::string m_key;
	std::unique_ptr<basic::Responder> m_responder;
};

///////////////////////////////////////////////////////////////////////////
// Subscriber

class SubscriberCreationException : public RemoteException {

public:
	SubscriberCreationException(const std::string& message);
};


class Subscriber {

	friend class cameo::Server;
	friend class cameo::App;

public:
	~Subscriber();
	void terminate();

	static std::unique_ptr<Subscriber> create(App & app, const std::string &publisherName);

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
	std::optional<std::string> receive() const;

	/**
	 * Returns a tuple of strings or nothing if the stream has finished.
	 */
	std::optional<std::tuple<std::string, std::string>> receiveTwoParts() const;

	void cancel();

	std::string toString() const;

private:
	Subscriber();
	void init(const App &app, const std::string &publisherName);
	void synchronize(const App & app);

	bool m_useProxy;
	std::string m_publisherName;
	std::string m_appName;
	int m_appId;
	Endpoint m_appEndpoint;
	std::unique_ptr<SubscriberImpl> m_impl;
	std::unique_ptr<Waiting> m_waiting;
	std::string m_key;
};

std::ostream& operator<<(std::ostream&, const cameo::coms::Publisher&);
std::ostream& operator<<(std::ostream&, const cameo::coms::Subscriber&);

}
}

#endif
