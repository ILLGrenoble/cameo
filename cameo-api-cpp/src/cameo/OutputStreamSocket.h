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

#ifndef CAMEO_OUTPUTSTREAMSOCKET_H_
#define CAMEO_OUTPUTSTREAMSOCKET_H_

#include <memory>
#include "Event.h"

namespace cameo {

class SocketImpl;
class WaitingImpl;

namespace application {

class Instance;

}

class Output {

public:
	Output(int id, const std::string& message, bool end);

	int getId() const;

	const std::string& getMessage() const;

	bool isEnd() const;

private:
	int m_id;
	std::string m_message;
	bool m_end;
};


class OutputStreamSocket {

	friend class Services;
	friend class application::Instance;

public:
	~OutputStreamSocket();

	std::unique_ptr<Output> receive();
	void cancel();

private:
	OutputStreamSocket(const std::string& streamString, const std::string& endOfStreamString, SocketImpl * impl);

	WaitingImpl * waiting();

	std::string m_streamString;
	std::string m_endOfStreamString;

	std::unique_ptr<SocketImpl> m_impl;
};

}

#endif
