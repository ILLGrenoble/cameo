/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

#ifndef CAMEO_COMS_BASIC_RESPONDERIMPL_H_
#define CAMEO_COMS_BASIC_RESPONDERIMPL_H_

#include <string>
#include <memory>

namespace cameo {
namespace coms {
namespace basic {

class Request;

class ResponderImpl {

public:
	virtual ~ResponderImpl() {}

	virtual void init(const std::string& responderIdentity) = 0;
	virtual int getResponderPort() = 0;
	virtual void cancel() = 0;
	virtual bool isCanceled() = 0;

	virtual std::unique_ptr<Request> receive() = 0;
	virtual void reply(const std::string& type, const std::string& response) = 0;
};

}
}
}

#endif