/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

#include "MultiResponderRouterZmq.h"

#include "This.h"
#include "ContextZmq.h"

namespace cameo {
namespace coms {
namespace multi {

ResponderRouterZmq::ResponderRouterZmq() :
	m_pollingTime{100},
	m_responderPort{0},
	m_canceled{false} {
}

ResponderRouterZmq::~ResponderRouterZmq() {
	terminate();
}

void ResponderRouterZmq::init(const std::string &responderIdentity, const std::string &dealerEndpoint) {

	// Create a socket ROUTER.
	ContextZmq * contextImpl {dynamic_cast<ContextZmq*>(This::getCom().getContext())};
	m_router.reset(new zmq::socket_t{contextImpl->getContext(), zmq::socket_type::router});

	// Set the identity.
	m_router->set(zmq::sockopt::routing_id, responderIdentity);

	// Connect to the proxy.
	Endpoint proxyEndpoint {This::getEndpoint().withPort(This::getCom().getResponderProxyPort())};
	m_router->connect(proxyEndpoint.toString());

	std::string endpointPrefix {"tcp://*:"};

	// Loop to find an available port for the responder.
	while (true) {

		int port {This::getCom().requestPort()};
		std::string repEndpoint {endpointPrefix + std::to_string(port)};

		try {
			m_router->bind(repEndpoint.c_str());
			m_responderPort = port;
			break;
		}
		catch (...) {
			This::getCom().setPortUnavailable(port);
		}
	}

	// Create a socket DEALER.
	m_dealer.reset(new zmq::socket_t{contextImpl->getContext(), zmq::socket_type::dealer});
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

		zmq::poll(&items[0], 2, std::chrono::milliseconds{m_pollingTime});

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
		This::getCom().releasePort(m_responderPort);
	}
}

}
}
}