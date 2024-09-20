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
#include <zmq.hpp>
#include <atomic>

namespace cameo {
namespace coms {

class PublisherZmq : public PublisherImpl {

public:
	PublisherZmq();
	virtual ~PublisherZmq();

	virtual void init(const std::string& publisherIdentity);
	virtual int getPublisherPort() const;

	virtual void sendSync();
	virtual void send(const std::string& data);
	virtual void sendTwoParts(const std::string& data1, const std::string& data2);
	virtual void setEnd();
	virtual bool hasEnded();
	virtual void terminate();

private:
	std::string createMessageType(int type);

	int m_publisherPort;
	std::string m_publisherIdentity;
	std::unique_ptr<zmq::socket_t> m_publisher;
	std::atomic_bool m_ended;
};

}
}

#endif
