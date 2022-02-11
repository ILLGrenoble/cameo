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

#include <string>
#include <iostream>
#include <zmq.hpp>

int main(int argc, char *argv[]) {

	if (argc < 3) {
		std::cout << "Usage: <front port> <back port>" << std::endl;
		return EXIT_FAILURE;
	}

	std::string frontPort{argv[1]};
	std::string frontAddress("tcp://*:");
	frontAddress += frontPort;

	std::string backPort{argv[2]};
	std::string backAddress("tcp://*:");
	backAddress += backPort;

	// Prepare our context and sockets.
	zmq::context_t context(1);
	zmq::socket_t frontend(context, ZMQ_XSUB);
	//set hwm

	try {
		frontend.bind(frontAddress);
	}
	catch (const std::exception& e) {
		std::cout << "Cannot bind socket to " << frontAddress << ": " << e.what() << std::endl;
		return EXIT_FAILURE;
	}

	zmq::socket_t backend(context, ZMQ_XPUB);
	//set hwm

	try {
		backend.bind(backAddress);
	}
	catch (const std::exception& e) {
		std::cout << "Cannot bind socket to " << backAddress << ": " << e.what() << std::endl;
		return EXIT_FAILURE;
	}

	zmq::proxy(frontend, backend);

	return EXIT_SUCCESS;
}
