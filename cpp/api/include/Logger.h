/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

#ifndef CAMEO_LOGGER_H_
#define CAMEO_LOGGER_H_

#include "Defines.h"
#include <functional>
#include <string>
#include <vector>
#include <mutex>

namespace cameo {

/**
 * Class providing a log callback for logging in external applications.
 */
class CAMEO_EXPORT Logger {

public:
	/**
	 * Callback type.
	 */
	typedef std::function<void (const std::string&)> CallbackType;

	/**
	 * Adds a log callback.
	 * \param callback The callback.
	 */
	static void add(CallbackType callback);

	/**
	 * Calls the callbacks with the line.
	 * \param line The line to be logged.
	 */
	static void log(const std::string& line);

private:
	static std::mutex m_mutex;
	static std::vector<CallbackType> m_callbacks;
};

}

#endif
