/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

#ifndef CAMEO_SYNCPUBLISHERZMQ_H_
#define CAMEO_SYNCPUBLISHERZMQ_H_

#include "PublisherZmq.h"
#include <mutex>

namespace cameo {
namespace coms {

class SyncPublisherZmq : public PublisherZmq {

public:
	SyncPublisherZmq();
	virtual ~SyncPublisherZmq();

	virtual void sendSync();
	virtual void send(const std::string& data);
	virtual void sendTwoParts(const std::string& data1, const std::string& data2);
	virtual void setEnd();

private:
	std::mutex m_mutex;
};

}
}

#endif