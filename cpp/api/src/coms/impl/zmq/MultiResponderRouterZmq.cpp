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

#include "MultiResponderRouterZmq.h"
#include "Application.h"
#include "Messages.h"
#include "JSON.h"
#include "BasicRequesterResponder.h"
#include "RequestSocket.h"
#include "ContextZmq.h"

namespace cameo {
namespace coms {
namespace multi {

ResponderRouterZmq::ResponderRouterZmq() :
	m_responderPort(0),
	m_canceled(false) {
}

ResponderRouterZmq::~ResponderRouterZmq() {
	terminate();
}

void ResponderRouterZmq::init(const std::string& responderIdentity, const std::string& dealerEndpoint) {

	m_responderIdentity = responderIdentity;

	// Create a socket ROUTER.
	ContextZmq* contextImpl = dynamic_cast<ContextZmq *>(application::This::getCom().getContext());
	m_router.reset(new zmq::socket_t(contextImpl->getContext(), zmq::socket_type::router));

	// Set the identity.
	m_router->setsockopt(ZMQ_IDENTITY, responderIdentity.data(), responderIdentity.size());

	// Connect to the proxy.
	Endpoint proxyEndpoint = application::This::getEndpoint().withPort(application::This::getCom().getResponderProxyPort());
	m_router->connect(proxyEndpoint.toString());

	std::string endpointPrefix("tcp://*:");

	// Loop to find an available port for the responder.
	while (true) {

		int port = application::This::getCom().requestPort();
		std::string repEndpoint = endpointPrefix + std::to_string(port);

		try {
			m_router->bind(repEndpoint.c_str());
			m_responderPort = port;
			break;
		}
		catch (...) {
			application::This::getCom().setPortUnavailable(port);
		}
	}

	// Create a socket DEALER.
	m_dealer.reset(new zmq::socket_t(contextImpl->getContext(), zmq::socket_type::dealer));
	m_dealer->bind(dealerEndpoint);
}

int ResponderRouterZmq::getResponderPort() {
	return m_responderPort;
}

void ResponderRouterZmq::cancel() {

//	json::StringObject jsonRequest;
//	jsonRequest.pushKey(message::TYPE);
//	jsonRequest.pushValue(message::CANCEL);
//
//	// Create a request socket connected directly to the responder.
//	std::unique_ptr<RequestSocket> requestSocket = application::This::getCom().createRequestSocket(application::This::getEndpoint().withPort(m_responderPort).toString(), m_responderIdentity);
//	requestSocket->requestJSON(jsonRequest.toString());

	std::cout << "ResponderRouterZmq::cancel TODO" << std::endl;
}

bool ResponderRouterZmq::isCanceled() {
	return m_canceled;
}

void ResponderRouterZmq::run() {

	// Connect work threads to client threads via a queue
    zmq::proxy(*m_router, *m_dealer, nullptr);
}

void ResponderRouterZmq::terminate() {

	if (m_router) {
		m_router.reset();
		m_dealer.reset();

		// Release the responder port.
		application::This::getCom().releasePort(m_responderPort);
	}
}

}
}
}

