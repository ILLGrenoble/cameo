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

#ifndef CAMEO_COMS_REQUESTERIMPL_H_
#define CAMEO_COMS_REQUESTERIMPL_H_

#include "Strings.h"
#include "TimeoutCounter.h"
#include <optional>

namespace cameo {
namespace coms {

class RequesterImpl {

public:
	virtual ~RequesterImpl() {}

	virtual void setPollingTime(int value) = 0;
	virtual void setTimeout(int value) = 0;

	virtual void init(const Endpoint& endpoint, const std::string& responderIdentity, const TimeoutCounter& timeoutCounter) = 0;
	virtual void send(const std::string& requestData) = 0;
	virtual void sendTwoParts(const std::string& requestData1, const std::string& requestData2) = 0;

	virtual std::optional<std::string> receive() = 0;

	virtual void cancel() = 0;
	virtual bool isCanceled() = 0;

	virtual bool hasTimedout() = 0;
};

}
}

#endif
