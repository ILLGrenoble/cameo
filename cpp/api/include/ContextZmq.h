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

#ifndef CAMEO_CONTEXTZMQ_H_
#define CAMEO_CONTEXTZMQ_H_

#include "Context.h"
#include <vector>
#include <memory>

/**
 * Namespace for ZeroMQ.
 */
namespace zmq {

	/**
	 * Forward declaration of the context class.
	 */
	class context_t;
}

namespace cameo {

/**
 * Class wrapping the ZeroMQ context.
 */
class ContextZmq : public Context {

public:
	/**
	 * Constructor.
	 */
	ContextZmq();

	/**
	 * Destructor.
	 */
	virtual ~ContextZmq();

	/**
	 * Gets the real ZeroMQ context.
	 * \return The context.
	 */
	zmq::context_t& getContext();

private:
	std::unique_ptr<zmq::context_t> m_context;
};

}

#endif
