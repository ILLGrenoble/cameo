/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

#ifndef CAMEO_SUBSCRIBERIMPL_H_
#define CAMEO_SUBSCRIBERIMPL_H_

#include "Strings.h"
#include <optional>
#include <tuple>

namespace cameo {
namespace coms {

class SubscriberImpl {

public:
	virtual ~SubscriberImpl() {}

	virtual void setPollingTime(int value) = 0;
	virtual void setTimeout(int value) = 0;

	virtual void init(int appId, const Endpoint& endpoint, const Endpoint& appStatusEndpoint, const std::string& publisherIdentity, bool checkApp) = 0;
	virtual bool sync(int timeout) = 0;

	virtual bool hasEnded() const = 0;
	virtual bool isCanceled() const = 0;

	virtual bool hasTimedout() = 0;

	virtual std::optional<std::string> receive() = 0;
	virtual std::optional<std::tuple<std::string, std::string>> receiveTwoParts() = 0;

	virtual void cancel() = 0;
};

}
}

#endif