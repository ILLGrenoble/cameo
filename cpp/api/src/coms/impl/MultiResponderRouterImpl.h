/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

#ifndef CAMEO_COMS_MULTI_RESPONDERROUTERIMPL_H_
#define CAMEO_COMS_MULTI_RESPONDERROUTERIMPL_H_

#include <string>
#include <memory>

namespace cameo {
namespace coms {
namespace multi {

class ResponderRouterImpl {

public:
	virtual ~ResponderRouterImpl() {}

	virtual void init(const std::string& responderIdentity, const std::string& dealerEndpoint) = 0;
	virtual void setPollingTime(int value) = 0;
	virtual int getResponderPort() = 0;
	virtual void cancel() = 0;
	virtual bool isCanceled() = 0;

	virtual void run() = 0;
};

}
}
}

#endif