/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
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
class CAMEO_EXPORT ContextZmq : public Context {

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