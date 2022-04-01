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

	virtual void init(int appId, const Endpoint& endpoint, const Endpoint& appStatusEndpoint, const std::string& publisherIdentity) = 0;

	virtual bool isEnded() const = 0;
	virtual bool isCanceled() const = 0;

	virtual std::optional<std::string> receive() = 0;
	virtual std::optional<std::tuple<std::string, std::string>> receiveTwoParts() = 0;

	virtual void cancel() = 0;
};

}
}

#endif
