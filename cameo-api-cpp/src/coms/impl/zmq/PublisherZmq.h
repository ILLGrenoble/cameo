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

#ifndef CAMEO_PUBLISHERZMQ_H_
#define CAMEO_PUBLISHERZMQ_H_

#include "../PublisherImpl.h"
#include <memory>
#include <string>
#include <zmq.hpp>

namespace cameo {

namespace application {
	class This;
}

namespace coms {

class PublisherZmq : public PublisherImpl {

public:
	PublisherZmq(const std::string& name, int numberOfSubscribers);
	~PublisherZmq();

	void init(int publisherPort, int synchronizerPort);

	bool waitForSubscribers();
	void cancelWaitForSubscribers();

	void sendBinary(const std::string& data);
	void send(const std::string& data);
	void sendTwoBinaryParts(const std::string& data1, const std::string& data2);
	void setEnd();
	bool isEnded();
	void terminate();

	void publish(const std::string& header, const char* data, std::size_t size);
	void publishTwoParts(const std::string& header, const char* data1, std::size_t size1, const char* data2, std::size_t size2);

private:
	zmq::message_t * responseToSyncRequest();
	zmq::message_t * responseToSubscribeRequest();
	zmq::message_t * responseToCancelRequest();
	zmq::message_t * responseToUnknownRequest();

	std::string createTerminatePublisherRequest(int id, const std::string& name) const;

	int m_synchronizerPort;
	std::string m_name;
	int m_numberOfSubscribers;
	std::unique_ptr<zmq::socket_t> m_publisher;
	bool m_ended;
};

}
}

#endif
