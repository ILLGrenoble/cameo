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
	This::handleStop([] {});

	int numberOfSubscribers = 1;

	std::unique_ptr<coms::Publisher> publisher;

	try {
		// Create the publisher.
		publisher = coms::Publisher::create("the-publisher");
		publisher->setWaitForSubscribers(numberOfSubscribers);

		std::cout << "Created publisher " << *publisher << std::endl;

		publisher->init();
	}
	catch (const InitException& e) {
		std::cout << "Publisher error" << std::endl;
		return EXIT_FAILURE;
	}

	// Set the state.
	This::setRunning();

	std::cout << "Synchronized with " << numberOfSubscribers << " subscriber(s)" << std::endl;

	int i = 0;
	while (!This::isStopping()) {

		// Send a message.
		publisher->send(std::string{"a message "} + std::to_string(i));
		i++;
		
		// Sleep for 1s.
		std::this_thread::sleep_for(std::chrono::seconds(1));
	}

	std::cout << "Finished the application" << std::endl;

	This::terminate();

	return EXIT_SUCCESS;
}
