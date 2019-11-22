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

#ifndef CAMEO_APPLICATIONIMPL_H_
#define CAMEO_APPLICATIONIMPL_H_

#include "HandlerImpl.h"
#include "ServicesImpl.h"

namespace cameo {

class EventStreamSocket;

namespace application {
	class This;
}

class ApplicationImpl : public ServicesImpl {

public:
	ApplicationImpl();
	virtual ~ApplicationImpl();

	void setEventSocket(std::unique_ptr<EventStreamSocket>& eventSocket);

	void handleStop(application::This * application, HandlerImpl::FunctionType stop);
	static void stoppingFunction(application::This * application, HandlerImpl::FunctionType stop);

	std::unique_ptr<EventStreamSocket> m_eventSocket;
	std::unique_ptr<HandlerImpl> m_stopHandler;
};

}

#endif
