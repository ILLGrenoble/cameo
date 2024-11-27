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
#include <thread>

int main(int argc, char *argv[]) {

	if (argc < 3) {
		std::cout << "Usage: <publisher port> <subscriber port>" << std::endl;
		return EXIT_FAILURE;
	}

	std::string pubPort{argv[1]};
	std::string pubAddress("tcp://*:");
	pubAddress += pubPort;

	std::string subPort{argv[2]};
	std::string subAddress("tcp://*:");
	subAddress += subPort;

	// Prepare our context and sockets.
	zmq::context_t context(1);
	zmq::socket_t frontend(context, ZMQ_XSUB);
	//set hwm

	try {
		frontend.bind(subAddress);
	}
	catch (const std::exception& e) {
		std::cout << "Cannot bind socket to " << subAddress << ": " << e.what() << std::endl;
		return EXIT_FAILURE;
	}

	zmq::socket_t backend(context, ZMQ_XPUB);
	//set hwm

	try {
		backend.bind(pubAddress);
	}
	catch (const std::exception& e) {
		std::cout << "Cannot bind socket to " << pubAddress << ": " << e.what() << std::endl;
		return EXIT_FAILURE;
	}

	zmq::proxy(frontend, backend);

	return EXIT_SUCCESS;
}