/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
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