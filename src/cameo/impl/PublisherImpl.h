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

#ifndef CAMEO_PUBLISHERIMPL_H_
#define CAMEO_PUBLISHERIMPL_H_

#include <memory>
#include <string>
#include <stdint.h>

#include "GenericWaitingImpl.h"
#include "zmq.hpp"

namespace cameo {

namespace application {
	class This;
}

class PublisherImpl {

public:
	PublisherImpl(const application::This * application, int publisherPort, int synchronizerPort, const std::string& name, int numberOfSubscribers);
	~PublisherImpl();

	const std::string& getName() const;
	const std::string& getApplicationName() const;
	int getApplicationId() const;
	const std::string& getApplicationEndpoint() const;

	bool waitForSubscribers();
	void cancelWaitForSubscribers();
	WaitingImpl * waiting();

	void sendBinary(const std::string& data);
	void send(const std::string& data);
	void send(const int32_t* data, std::size_t size);
	void send(const int64_t* data, std::size_t size);
	void send(const float* data, std::size_t size);
	void send(const double* data, std::size_t size);
	void sendTwoBinaryParts(const std::string& data1, const std::string& data2);
	void setEnd();
	bool hasEnded();
	void terminate();

	void publish(const std::string& header, const char* data, std::size_t size);
	void publishTwoParts(const std::string& header, const char* data1, std::size_t size1, const char* data2, std::size_t size2);

	zmq::message_t * processInitCommand();
	zmq::message_t * processSubscribePublisherCommand();
	zmq::message_t * processCancelPublisherSyncCommand();

	const application::This * m_application;
	int m_publisherPort;
	int m_synchronizerPort;
	std::string m_name;
	int m_numberOfSubscribers;
	std::auto_ptr<zmq::socket_t> m_publisher;
	bool m_ended;

	static const std::string SYNC;
	static const std::string STREAM;
	static const std::string ENDSTREAM;
};

}

#endif
