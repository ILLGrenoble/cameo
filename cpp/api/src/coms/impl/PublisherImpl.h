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

#ifndef CAMEO_PUBLISHERIMPL_H_
#define CAMEO_PUBLISHERIMPL_H_

#include <string>

namespace cameo {

namespace application {
	class This;
}

namespace coms {

class PublisherImpl {

public:
	static const int SUBSCRIBE_PUBLISHER = 100;

	virtual ~PublisherImpl() {}

	virtual void init(const std::string& publisherIdentity, int numberOfSubscribers) = 0;

	virtual int getPublisherPort() const = 0;
	virtual int getSynchronizerPort() const = 0;

	virtual bool waitForSubscribers() = 0;
	virtual void cancelWaitForSubscribers() = 0;

	virtual void sendBinary(const std::string& data) = 0;
	virtual void send(const std::string& data) = 0;
	virtual void sendTwoBinaryParts(const std::string& data1, const std::string& data2) = 0;
	virtual void setEnd() = 0;
	virtual bool isEnded() = 0;
	virtual void terminate() = 0;
};

}
}

#endif
