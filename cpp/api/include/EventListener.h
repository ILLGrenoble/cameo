/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

#ifndef CAMEO_EVENTLISTENER_H_
#define CAMEO_EVENTLISTENER_H_

#include "ConcurrentQueue.h"
#include <string>

namespace cameo {

class Event;

/**
 * Class defining an event listener.
 * The application name is not required, in that case all messages are received.
 */
class CAMEO_EXPORT EventListener {

public:
	/**
	 * Constructor.
	 */
	EventListener();

	/**
	 * Destructor.
	 */
	virtual ~EventListener();

	/**
	 * Sets the name of the listener i.e. the application name.
	 * It is used to filter the messages in the event thread because when the listener is registered, the application id is not known.
	 * \param name The application name.
	 */
	void setName(const std::string& name);

	/**
	 * Gets the application name.
	 * \return The name.
	 */
	const std::string& getName() const;

	/**
	 * Pushes the event on the queue.
	 * \param event The event.
	 */
	void pushEvent(std::unique_ptr<Event>& event);

	/**
	 * Pops the event from the queue.
	 * \param blocking True if the call is blocking.
	 */
	std::unique_ptr<Event> popEvent(bool blocking = true, int timeout = -1);

	/**
	 * Pushes a CancelEvent with application id.
	 * \param id The application id.
	 */
	void cancel(int id);

protected:
	std::string m_name;
	ConcurrentQueue<Event> m_eventQueue;
};

}

#endif