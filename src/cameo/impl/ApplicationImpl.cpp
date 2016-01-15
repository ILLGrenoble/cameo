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

#include "../../cameo/impl/ApplicationImpl.h"

#include "../EventStreamSocket.h"
#include <memory>
#include <boost/bind.hpp>

#include <iostream>
#include "../../cameo/Application.h"

using namespace std;

namespace cameo {

ApplicationImpl::ApplicationImpl() :
	ServicesImpl() {
}

ApplicationImpl::~ApplicationImpl() {

	// Cancel the event socket in case it was started with a stop handler.
	if (m_eventSocket.get() != 0) {
		m_eventSocket->cancel();
	}
}

void ApplicationImpl::setEventSocket(std::auto_ptr<EventStreamSocket>& eventSocket) {
	m_eventSocket = eventSocket;
}

void ApplicationImpl::handleStop(application::This * application, HandlerImpl::FunctionType stop) {
	m_stopHandler = auto_ptr<HandlerImpl>(new HandlerImpl(boost::bind(&ApplicationImpl::stoppingFunction, application, stop)));
}

void ApplicationImpl::stoppingFunction(application::This * application, HandlerImpl::FunctionType stop) {

	application::State state = application->waitForStop();

	// Only stop in case of STOPPING.
	if (state == application::STOPPING) {
		stop();
	}
}

}