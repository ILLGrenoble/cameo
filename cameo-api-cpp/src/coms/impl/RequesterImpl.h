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

#ifndef CAMEO_REQUESTERIMPL_H_
#define CAMEO_REQUESTERIMPL_H_

#include "../../base/Waiting.h"
#include "Strings.h"
#include <string>

namespace cameo {
namespace coms {

class RequesterImpl {

public:
	static const std::string REQUESTER_PREFIX;

	virtual ~RequesterImpl() {}

	virtual void init(const Endpoint& endpoint, int requesterPort, int responderPort, const std::string& name) = 0;
	virtual void sendBinary(const std::string& requestData) = 0;
	virtual void send(const std::string& requestData) = 0;
	virtual void sendTwoBinaryParts(const std::string& requestData1, const std::string& requestData2) = 0;

	virtual std::optional<std::string> receiveBinary() = 0;
	virtual std::optional<std::string> receive() = 0;

	virtual void cancel() = 0;
	virtual bool isCanceled() = 0;
	virtual void terminate() = 0;
};

}
}

#endif
