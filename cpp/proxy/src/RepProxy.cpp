/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

#include <string>
#include <iostream>
#include <zmq.hpp>

int main(int argc, char *argv[]) {

	if (argc < 2) {
		std::cout << "Usage: <port>" << std::endl;
		return EXIT_FAILURE;
	}

	std::string port{argv[1]};
	std::string address("tcp://*:");
	address += port;

	// Prepare our context and socket.
	zmq::context_t context(1);
	zmq::socket_t router(context, zmq::socket_type::router);
	router.set(zmq::sockopt::routing_id, "R");

	try {
		router.bind(address);
	}
	catch (const std::exception& e) {
		std::cout << "Cannot bind socket to " << address << ": " << e.what() << std::endl;
		return EXIT_FAILURE;
	}

	// Initialize poll set.
	zmq::pollitem_t items[] = { { router, 0, ZMQ_POLLIN, 0 } };
	zmq::const_buffer emptyBuffer;

	while (true) {
		zmq::poll(&items[0], 1, std::chrono::milliseconds{-1});

		if (items[0].revents & ZMQ_POLLIN) {

			zmq::message_t fromIdentity, empty, toIdentity, message;

			// From identity.
			if (!router.recv(fromIdentity, zmq::recv_flags::none)) {
				continue;
			}

			if (!fromIdentity.more()) {
				continue;
			}

			// Empty.
			if (!router.recv(empty, zmq::recv_flags::none)) {
				continue;
			}

			if (!empty.more()) {
				continue;
			}

			// To identity.
			if (!router.recv(toIdentity, zmq::recv_flags::none)) {
				continue;
			}

			if (!toIdentity.more()) {
				continue;
			}

			// Empty.
			if (!router.recv(empty, zmq::recv_flags::none)) {
				continue;
			}

			if (!empty.more()) {
				continue;
			}

			// Message content.
			if (!router.recv(message, zmq::recv_flags::none)) {
				continue;
			}

			// This part will be removed and replaced by the router identity once the message is sent.
			router.send(toIdentity, zmq::send_flags::sndmore);
			router.send(emptyBuffer, zmq::send_flags::sndmore);
			router.send(fromIdentity, zmq::send_flags::sndmore);
			router.send(emptyBuffer, zmq::send_flags::sndmore);

			// Loop on the parts.
			bool more = message.more();

			if (more) {
				router.send(message, zmq::send_flags::sndmore);

				while (true) {

					zmq::message_t messagePart;

					if (!router.recv(messagePart, zmq::recv_flags::none)) {
						continue;
					}

					if (messagePart.more()) {
						router.send(messagePart, zmq::send_flags::sndmore);
					}
					else {
						router.send(messagePart, zmq::send_flags::none);
						break;
					}
				}
			}
			else {
				router.send(message, zmq::send_flags::none);
			}
		}
	}

	return EXIT_SUCCESS;
}