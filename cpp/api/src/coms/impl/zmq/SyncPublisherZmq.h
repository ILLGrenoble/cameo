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
