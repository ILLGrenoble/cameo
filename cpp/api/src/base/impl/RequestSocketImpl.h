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
