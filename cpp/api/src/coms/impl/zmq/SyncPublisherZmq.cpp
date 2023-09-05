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

#include "SyncPublisherZmq.h"

namespace cameo {
namespace coms {

SyncPublisherZmq::SyncPublisherZmq() {
}

SyncPublisherZmq::~SyncPublisherZmq() {
}

void SyncPublisherZmq::sendSync() {

	std::unique_lock<std::mutex> lock(m_mutex);

	PublisherZmq::sendSync();
}

void SyncPublisherZmq::send(const std::string& data) {

	std::unique_lock<std::mutex> lock(m_mutex);

	PublisherZmq::send(data);
}

void SyncPublisherZmq::sendTwoParts(const std::string& data1, const std::string& data2) {

	std::unique_lock<std::mutex> lock(m_mutex);

	PublisherZmq::sendTwoParts(data1, data2);
}

void SyncPublisherZmq::setEnd() {

	std::unique_lock<std::mutex> lock(m_mutex);

	PublisherZmq::setEnd();


}

}
}

