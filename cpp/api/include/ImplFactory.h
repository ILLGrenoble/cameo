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

#ifndef CAMEO_IMPLFACTORY_H_
#define CAMEO_IMPLFACTORY_H_

#include "Defines.h"
#include <memory>
#include <mutex>

namespace cameo {

class Context;
class RequestSocketImpl;
class StreamSocketImpl;

namespace coms {

namespace basic {
	class ResponderImpl;
}

namespace multi {
	class ResponderImpl;
	class ResponderRouterImpl;
}

class RequesterImpl;
class PublisherImpl;
class SubscriberImpl;

}

/**
 * Factory for the implementation objects.
 * This implementation of the factory generates ZeroMQ implementations.
 */
class CAMEO_EXPORT ImplFactory {

public:
	/**
	 * Gets the default context implementation.
	 * \return The default Context object.
	 */
	static std::shared_ptr<Context> getDefaultContext();

	/**
	 * Terminates the default context implementation.
	 */
	static void terminateDefaultContext();

	/**
	 * Creates a context implementation.
	 * \return A new Context object.
	 */
	static std::unique_ptr<Context> createContext();

	/**
	 * Creates a request socket implementation.
	 * \param context The context.
	 * \param endpoint The endpoint.
	 * \param responderIdentity The responder identity.
	 * \return A new RequestSocketImpl object.
	 */
	static std::unique_ptr<RequestSocketImpl> createRequestSocket(Context * context, const std::string& endpoint, const std::string& responderIdentity);

	/**
	 * Creates an event stream socket implementation.
	 * \return A new StreamSocketImpl object.
	 */
	static std::unique_ptr<StreamSocketImpl> createEventStreamSocket();

	/**
	 * Creates an event stream socket with name implementation.
	 * \param name The name.
	 * \return A new StreamSocketImpl object.
	 */
	static std::unique_ptr<StreamSocketImpl> createOutputStreamSocket(const std::string& name);

	/**
	 * Creates a publisher implementation.
	 * \param sync True if is a synchronized publisher.
	 * \return A new coms::PublisherImpl object.
	 */
	static std::unique_ptr<coms::PublisherImpl> createPublisher(bool sync);

	/**
	 * Creates a subscriber implementation.
	 * \return A new coms::SubscriberImpl object.
	 */
	static std::unique_ptr<coms::SubscriberImpl> createSubscriber();

	/**
	 * Creates a basic responder implementation.
	 * \return A new coms::basic::ResponderImpl object.
	 */
	static std::unique_ptr<coms::basic::ResponderImpl> createBasicResponder();

	/**
	 * Creates a multi responder router implementation.
	 * \return A new coms::multi::ResponderRouterImpl object.
	 */
	static std::unique_ptr<coms::multi::ResponderRouterImpl> createMultiResponderRouter();

	/**
	 * Creates a multi responder implementation.
	 * \return A new coms::multi::ResponderImpl object.
	 */
	static std::unique_ptr<coms::multi::ResponderImpl> createMultiResponder();

	/**
	 * Creates a requester implementation.
	 * \return A new coms::RequesterImpl object.
	 */
	static std::unique_ptr<coms::RequesterImpl> createRequester();

private:
	static std::mutex m_mutex;
	static std::shared_ptr<Context> m_defaultContext;
};

}

#endif
