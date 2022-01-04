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
	virtual ~PublisherImpl() {}

	virtual void init(int publisherPort, int synchronizerPort) = 0;

	virtual bool waitForSubscribers() = 0;
	virtual void cancelWaitForSubscribers() = 0;

	virtual void sendBinary(const std::string& data) = 0;
	virtual void send(const std::string& data) = 0;
	virtual void sendTwoBinaryParts(const std::string& data1, const std::string& data2) = 0;
	virtual void setEnd() = 0;
	virtual bool isEnded() = 0;
	virtual void terminate() = 0;

	virtual void publish(const std::string& header, const char* data, std::size_t size) = 0;
	virtual void publishTwoParts(const std::string& header, const char* data1, std::size_t size1, const char* data2, std::size_t size2) = 0;
};

}
}

#endif
