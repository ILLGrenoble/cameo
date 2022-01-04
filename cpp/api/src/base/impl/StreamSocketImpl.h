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

#ifndef CAMEO_EVENTSTREAMSOCKETIMPL_H_
#define CAMEO_EVENTSTREAMSOCKETIMPL_H_

#include "Strings.h"
#include "../RequestSocket.h"
#include <string>

namespace cameo {

class Context;

class StreamSocketImpl {

public:
	virtual ~StreamSocketImpl() {}

	virtual void init(Context * context, const Endpoint& endpoint, RequestSocket * requestSocket) = 0;
	virtual void send(const std::string& data) = 0;
	virtual std::string receive(bool blocking = true) = 0;
	virtual void cancel() = 0;
	virtual void close() = 0;
};

}

#endif
