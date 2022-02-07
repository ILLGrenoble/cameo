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

#include <memory>

namespace cameo {

class Context;
class RequestSocketImpl;
class StreamSocketImpl;

namespace coms {

namespace legacy {
	class ResponderImpl;
	class RequesterImpl;
}

namespace basic {
	class ResponderImpl;
	class RequesterImpl;
}

class PublisherImpl;
class SubscriberImpl;

}

class ImplFactory {

public:
	static std::unique_ptr<Context> createContext();
	static std::unique_ptr<RequestSocketImpl> createRequestSocket(Context * context, const std::string& endpoint);
	static std::unique_ptr<StreamSocketImpl> createEventStreamSocket();
	static std::unique_ptr<StreamSocketImpl> createOutputStreamSocket(const std::string& name);

	static std::unique_ptr<coms::PublisherImpl> createPublisher(const std::string& name, int numberOfSubscribers);
	static std::unique_ptr<coms::SubscriberImpl> createSubscriber();

	static std::unique_ptr<coms::legacy::ResponderImpl> createLegacyResponder();
	static std::unique_ptr<coms::legacy::RequesterImpl> createLegacyRequester();
	static std::unique_ptr<coms::basic::ResponderImpl> createBasicResponder();
	static std::unique_ptr<coms::basic::RequesterImpl> createBasicRequester();
};

}

#endif
