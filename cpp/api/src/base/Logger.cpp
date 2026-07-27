/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

#include "Logger.h"

namespace cameo {

std::mutex Logger::m_mutex;
std::vector<Logger::CallbackType> Logger::m_callbacks;


void Logger::add(CallbackType callback) {

	std::lock_guard<std::mutex> lock {m_mutex};

	m_callbacks.push_back(callback);
}

void Logger::log(const std::string& line) {

	std::lock_guard<std::mutex> lock {m_mutex};

	for (auto& c : m_callbacks) {
		c(line);
	}
}

}
