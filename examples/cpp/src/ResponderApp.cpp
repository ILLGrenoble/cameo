/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

#include <cameo/api/cameo.h>
#include <iostream>

using namespace cameo;

int main(int argc, char *argv[]) {

	// Initialize cameo.
	This::init(argc, argv);

	// Define the stop handler to properly stop.
	This::handleStop([] {
		This::cancelAll();
	});

	// Create the responder.
	std::unique_ptr<coms::basic::Responder> responder;

	try {
		responder = coms::basic::Responder::create("the-responder");
		responder->init();
		std::cout << "Created and initialized responder " << *responder << std::endl;
	}
	catch (const InitException& e) {
		std::cout << "Responder error" << std::endl;
		return EXIT_FAILURE;
	}

	// Set the state.
	This::setRunning();

	while (true) {
		
		// Receive the simple request.
		std::unique_ptr<coms::basic::Request> request = responder->receive();
		if (!request) {
			std::cout << "Responder is canceled" << std::endl;
			break;
		}

		std::cout << "Received request " << request->get() << std::endl;

		// Reply.
		request->reply("done");
	}

	std::cout << "Finished the application" << std::endl;

	return EXIT_SUCCESS;
}