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

#ifndef CAMEO_CONNECTIONCHECKER_H_
#define CAMEO_CONNECTIONCHECKER_H_

#include <functional>
#include <thread>
#include <memory>
#include <string>
#include <map>

namespace cameo {

class Server;
class TimeCondition;

/**
 * Class providing a simple connection checker.
 */
class ConnectionChecker {

	friend class Server;

public:
	typedef std::function<void (bool)> FunctionType;

	ConnectionChecker(Server * server, FunctionType handler);
	~ConnectionChecker();

private:
	void startThread(int timeoutMs, int pollingTimeMs);
	void stopThread();

	void loop(int timeoutMs, int pollingTimeMs);

	Server * m_server;
	std::unique_ptr<TimeCondition> m_waitCondition;
	FunctionType m_function;
	std::unique_ptr<std::thread> m_thread;
};

}

#endif
