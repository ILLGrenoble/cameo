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

#ifndef CAMEO_EVENTLISTENER_H_
#define CAMEO_EVENTLISTENER_H_

#include "ConcurrentQueue.h"
#include <string>

namespace cameo {

class Event;

class EventListener {

public:
	EventListener();
	virtual ~EventListener();

	void setName(const std::string& name);
	const std::string& getName() const;

	void pushEvent(std::unique_ptr<Event>& event);
	std::unique_ptr<Event> popEvent(bool blocking);
	std::unique_ptr<Event> popEvent();
	void cancel(int id);

protected:
	std::string m_name;
	ConcurrentQueue<Event> m_eventQueue;
};

}

#endif
