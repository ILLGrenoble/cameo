/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

#ifndef CAMEO_OUTPUTSTREAMSOCKET_H_
#define CAMEO_OUTPUTSTREAMSOCKET_H_

#include "Cancelable.h"
#include "Event.h"
#include "Strings.h"
#include <memory>
#include <optional>
#include <atomic>

namespace cameo {

class Context;
class RequestSocket;
class StreamSocketImpl;
class App;

/**
 * Class defining an output for application streams.
 */
class CAMEO_EXPORT Output {

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
class CAMEO_EXPORT OutputStreamSocket : public Cancelable {

	friend class Server;
	friend class App;

	void setApplicationId(int id);

public:
	/**
	 * Destructor.
	 */
	~OutputStreamSocket();

	/**
	 * Cancels the receive() waiting call in another thread.
	 */
	void cancel() override;

	/**
	 * Returns true if the stream has been canceled.
	 * \return True if canceled.
	 */
	bool isCanceled() const override;

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
	 * Returns true if the stream ended.
	 * \return True if ended.
	 */
	bool hasEnded() const;

private:
	OutputStreamSocket(const std::string& name);
	void init(Context * context, const Endpoint& endpoint, RequestSocket * requestSocket);

	int m_applicationId;
	std::atomic_bool m_ended;
	std::atomic_bool m_canceled;

	std::unique_ptr<StreamSocketImpl> m_impl;
};

}

#endif