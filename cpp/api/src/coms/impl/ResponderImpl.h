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

#ifndef CAMEO_RESPONDERIMPL_H_
#define CAMEO_RESPONDERIMPL_H_

#include <string>
#include <memory>

namespace cameo {
namespace coms {

class Request;

class ResponderImpl {

public:
	virtual ~ResponderImpl() {}

	virtual void init(int responderPort) = 0;
	virtual void cancel() = 0;
	virtual bool isCanceled() = 0;

	virtual std::unique_ptr<Request> receive() = 0;

	static const std::string RESPONDER_PREFIX;
};

}
}

#endif
