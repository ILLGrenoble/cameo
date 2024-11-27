/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

#ifndef CAMEO_REQUESTSOCKETIMPL_H_
#define CAMEO_REQUESTSOCKETIMPL_H_

#include <string>

namespace cameo {

class RequestSocketImpl {

public:
	virtual ~RequestSocketImpl() {}

	virtual void setTimeout(int timeout) = 0;

	virtual std::string request(const std::string& request, int overrideTimeout) = 0;
	virtual std::string request(const std::string& requestPart1, const std::string& requestPart2, int overrideTimeout) = 0;
	virtual std::string request(const std::string& requestPart1, const std::string& requestPart2, const std::string& requestPart3, int overrideTimeout) = 0;
};

}

#endif