/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

#include "Heartbeat.h"

namespace cameo {

Heartbeat::Heartbeat(int period) :
	m_period(period) {
}

Heartbeat::~Heartbeat() {
}

void Heartbeat::start() {

	if (!m_pingThread) {
		m_pingThread = std::make_unique<std::thread>([this]() {

			while (true) {

				std::unique_lock<std::mutex> lock(m_mutex);
				std::cv_status status = m_pingCondition.wait_for(lock, std::chrono::seconds(m_period));

				if (status == std::cv_status::timeout) {
					pingAll();
				}
				else {
					break;
				}
			}
		});
	}
}

void Heartbeat::terminate() {
	// Join the ping thread.
	if (m_pingThread) {
		m_pingCondition.notify_one();
		m_pingThread->join();
	}
}

}
