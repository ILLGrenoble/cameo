/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

#ifndef CAMEO_EVENTSTREAMSOCKETIMPL_H_
#define CAMEO_EVENTSTREAMSOCKETIMPL_H_

#include "Strings.h"
#include "RequestSocket.h"

namespace cameo {

class Context;

class StreamSocketImpl {

public:
	virtual ~StreamSocketImpl() {}

	virtual void init(Context * context, const Endpoint& endpoint, RequestSocket * requestSocket) = 0;
	virtual void send(const std::string& data) = 0;
	virtual std::string receive(bool blocking = true) = 0;
	virtual void cancel() = 0;
	virtual void terminate() = 0;
};

}

#endif