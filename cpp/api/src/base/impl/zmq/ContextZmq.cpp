/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

#include "ContextZmq.h"
#include <zmq.hpp>

#include <iostream>

namespace cameo {

ContextZmq::ContextZmq() : Context(),
	m_context{new zmq::context_t{1}} {
}

ContextZmq::~ContextZmq() {	
	m_context.reset();
}

zmq::context_t& ContextZmq::getContext() {
	return *m_context.get();
}

}