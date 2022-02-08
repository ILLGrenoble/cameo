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

#include "ImplFactory.h"

#include "../base/impl/zmq/ContextZmq.h"
#include "../base/impl/zmq/RequestSocketZmq.h"
#include "../base/impl/zmq/EventStreamSocketZmq.h"
#include "../base/impl/zmq/OutputStreamSocketZmq.h"
#include "../coms/impl/zmq/PublisherZmq.h"
#include "../coms/impl/zmq/SubscriberZmq.h"
#include "../coms/impl/zmq/LegacyResponderZmq.h"
#include "../coms/impl/zmq/LegacyRequesterZmq.h"
#include "../coms/impl/zmq/BasicResponderZmq.h"
#include "../coms/impl/zmq/BasicRequesterZmq.h"

namespace cameo {

std::unique_ptr<Context> ImplFactory::createContext() {
	return std::make_unique<ContextZmq>();
}

std::unique_ptr<RequestSocketImpl> ImplFactory::createRequestSocket(Context * context, const std::string& endpoint, const std::string& responderIdentity) {
	return std::make_unique<RequestSocketZmq>(context, endpoint, responderIdentity);
}

std::unique_ptr<StreamSocketImpl> ImplFactory::createEventStreamSocket() {
	return std::make_unique<EventStreamSocketZmq>();
}

std::unique_ptr<StreamSocketImpl> ImplFactory::createOutputStreamSocket(const std::string& name) {
	return std::make_unique<OutputStreamSocketZmq>(name);
}

std::unique_ptr<coms::PublisherImpl> ImplFactory::createPublisher(const std::string& name, int numberOfSubscribers) {
	return std::make_unique<coms::PublisherZmq>(name, numberOfSubscribers);
}

std::unique_ptr<coms::SubscriberImpl> ImplFactory::createSubscriber() {
	return std::make_unique<coms::SubscriberZmq>();
}

std::unique_ptr<coms::legacy::ResponderImpl> ImplFactory::createLegacyResponder() {
	return std::make_unique<coms::legacy::ResponderZmq>();
}

std::unique_ptr<coms::legacy::RequesterImpl> ImplFactory::createLegacyRequester() {
	return std::make_unique<coms::legacy::RequesterZmq>();
}

std::unique_ptr<coms::basic::ResponderImpl> ImplFactory::createBasicResponder() {
	return std::make_unique<coms::basic::ResponderZmq>();
}

std::unique_ptr<coms::basic::RequesterImpl> ImplFactory::createBasicRequester() {
	return std::make_unique<coms::basic::RequesterZmq>();
}

}
