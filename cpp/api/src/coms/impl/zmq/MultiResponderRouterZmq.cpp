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
#include "ContextZmq.h"

namespace cameo {
namespace coms {
namespace multi {

ResponderRouterZmq::ResponderRouterZmq() :
	m_pollingTime(100),	m_responderPort(0), m_canceled(false) {
}

ResponderRouterZmq::~ResponderRouterZmq() {
	terminate();
}

void ResponderRouterZmq::init(const std::string &responderIdentity, const std::string &dealerEndpoint) {

	// Create a socket ROUTER.
	ContextZmq *contextImpl = dynamic_cast<ContextZmq*>(application::This::getCom().getContext());
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

void ResponderRouterZmq::setPollingTime(int value) {
	m_pollingTime = value;
}

int ResponderRouterZmq::getResponderPort() {
	return m_responderPort;
}

void ResponderRouterZmq::cancel() {
	m_canceled = true;
}

bool ResponderRouterZmq::isCanceled() {
	return m_canceled;
}

void ResponderRouterZmq::run() {

	// Poll to check if the router has been canceled.
	zmq::pollitem_t items[] = { { *m_router, 0, ZMQ_POLLIN, 0 }, { *m_dealer, 0, ZMQ_POLLIN, 0 } };

	// Switch messages between sockets.
	while (true) {

		zmq::poll(&items[0], 2, m_pollingTime);

		if (items[0].revents & ZMQ_POLLIN) {

			while (true) {
				zmq::message_t message;

				if (!m_router->recv(message, zmq::recv_flags::none).has_value()) {
					continue;
				}

				if (message.more()) {
					m_dealer->send(message, zmq::send_flags::sndmore);
				}
				else {
					m_dealer->send(message, zmq::send_flags::none);
					break;
				}
			}
		}
		if (items[1].revents & ZMQ_POLLIN) {
			while (true) {
				zmq::message_t message;

				if (!m_dealer->recv(message, zmq::recv_flags::none).has_value()) {
					continue;
				}

				if (message.more()) {
					m_router->send(message, zmq::send_flags::sndmore);
				}
				else {
					m_router->send(message, zmq::send_flags::none);
					break;
				}
			}
		}

		if (m_canceled) {
			break;
		}
	}
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

