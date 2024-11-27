/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
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