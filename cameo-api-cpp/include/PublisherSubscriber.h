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

#include "Application.h"
#include "PublisherCreationException.h"
#include "SubscriberCreationException.h"

namespace cameo {
namespace coms {

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
	const std::string& getApplicationName() const;
	int getApplicationId() const;
	std::string getApplicationEndpoint() const;

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

private:
	Publisher(int publisherPort, int synchronizerPort, const std::string &name, int numberOfSubscribers);

	static std::string createCreatePublisherRequest(int id, const std::string &name, int numberOfSubscribers);

	std::unique_ptr<PublisherImpl> m_impl;
	std::unique_ptr<WaitingImpl> m_waiting;
};

///////////////////////////////////////////////////////////////////////////
// Subscriber

class Subscriber {

	friend class cameo::Server;
	friend class cameo::application::Instance;

public:
	~Subscriber();

	static std::unique_ptr<Subscriber> create(application::Instance &instance, const std::string &publisherName);

	const std::string& getPublisherName() const;
	const std::string& getInstanceName() const;
	int getInstanceId() const;
	const std::string& getInstanceEndpoint() const;

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
	Subscriber(Server *server, int publisherPort, int synchronizerPort, const std::string &publisherName, int numberOfSubscribers, const std::string &instanceName, int instanceId, const std::string &instanceEndpoint, const std::string &statusEndpoint);
	void init();

	static std::unique_ptr<Subscriber> createSubscriber(application::Instance &instance, const std::string &publisherName, const std::string &instanceName);
	static std::string createConnectPublisherRequest(int id, const std::string& publisherName);

	std::unique_ptr<SubscriberImpl> m_impl;
	std::unique_ptr<WaitingImpl> m_waiting;
};

std::ostream& operator<<(std::ostream&, const cameo::coms::Publisher&);
std::ostream& operator<<(std::ostream&, const cameo::coms::Subscriber&);

}
}

