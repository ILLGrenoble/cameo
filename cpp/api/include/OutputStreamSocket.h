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
#include "Strings.h"
#include <memory>
#include <optional>

namespace cameo {

class Context;
class RequestSocket;
class StreamSocketImpl;
class App;

/**
 * Class defining an output for application streams.
 */
class Output {

	friend class OutputStreamSocket;

public:
	/**
	 * Constructor.
	 */
	Output();

	/**
	 * Gets the id.
	 * \return The id.
	 */
	int getId() const;

	/**
	 * Gets the message.
	 * \return The message.
	 */
	const std::string& getMessage() const;

	/**
	 * Returns the end of line.
	 * \return True if end of line.
	 */
	bool isEndOfLine() const;

	/**
	 * Returns a string representation of the output.
	 * \return The string representation.
	 */
	std::string toString() const;

private:
	int m_id;
	std::string m_message;
	bool m_endOfLine;
};

/**
 * Class defining an output stream socket.
 */
class OutputStreamSocket {

	friend class Server;
	friend class App;

	void setApplicationId(int id);

public:
	/**
	 * Destructor.
	 */
	~OutputStreamSocket();

	/**
	 * Terminates the communication.
	 */
	void terminate();

	/**
	 * Receives an output.
	 * \return An Output object.
	 */
	std::optional<Output> receive();

	/**
	 * Cancels the receive() waiting call in another thread.
	 */
	void cancel();

	/**
	 * Returns true if the stream ended.
	 * \return True if ended.
	 */
	bool hasEnded() const;

	/**
	 * Returns true if the stream has been canceled.
	 * \return True if canceled.
	 */
	bool isCanceled() const;

	/**
	 * Returns a string representation of this application.
	 * \return The string representation.
	 */
	std::string toString() const;

private:
	OutputStreamSocket(const std::string& name);
	void init(Context * context, const Endpoint& endpoint, RequestSocket * requestSocket);

	int m_applicationId;
	bool m_ended;
	bool m_canceled;

	std::unique_ptr<StreamSocketImpl> m_impl;
};

}

#endif
