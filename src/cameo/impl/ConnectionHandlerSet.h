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

#ifndef CAMEO_CONNECTIONHANDLERSET_H_
#define CAMEO_CONNECTIONHANDLERSET_H_

#include "TimeCondition.h"
#include <boost/function.hpp>
#include <boost/thread.hpp>
#include <boost/bind.hpp>
#include <memory>
#include <string>
#include <map>

namespace cameo {

class Server;

/**
 * Class containing a set of connection handler objects.
 * It is protected with a mutex because the class must be thread-safe.
 */
class ConnectionHandlerSet {

public:
	typedef boost::function<void (bool)> FunctionType;

	ConnectionHandlerSet(Server * server);
	~ConnectionHandlerSet();

	void add(std::string const & name, FunctionType handler);
	bool remove(std::string const & name);

	void startThread(int timeoutMs, int pollingTimeMs);
	void stopThread();

private:
	void apply(bool available);
	void loop(int timeoutMs, int pollingTimeMs);

	Server * m_server;
	TimeCondition m_waitCondition;
	boost::mutex m_mutex;
	std::map<std::string, FunctionType> m_set;
	std::auto_ptr<boost::thread> m_thread;
};

}

#endif
