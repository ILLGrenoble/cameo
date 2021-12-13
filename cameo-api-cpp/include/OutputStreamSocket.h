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

#include "Event.h"
#include <memory>
#include <optional>

namespace cameo {

class StreamSocketImpl;
class WaitingImpl;

namespace application {

class Instance;

}

class Output {

	friend class OutputStreamSocket;

public:
	Output();

	int getId() const;
	const std::string& getMessage() const;
	bool isEndOfLine() const;

private:
	int m_id;
	std::string m_message;
	bool m_endOfLine;
};


class OutputStreamSocket {

	friend class Server;
	friend class application::Instance;

	void setApplicationId(int id);

public:
	~OutputStreamSocket();

	std::optional<Output> receive();
	void cancel();
	bool isEnded() const;
	bool isCanceled() const;

private:
	OutputStreamSocket(StreamSocketImpl * impl);

	int m_applicationId;
	bool m_ended;
	bool m_canceled;

	std::unique_ptr<StreamSocketImpl> m_impl;
};

}

#endif
