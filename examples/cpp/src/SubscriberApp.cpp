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
		// Cancel the subscriber.
		This::cancelAll();
	});

	// Parameters: publisher endpoint, language.
	if (argc < 4) {
		std::cout << "Parameters: <publisher endpoint> <language>" << std::endl;
		return EXIT_FAILURE;
	}

	std::string serverEndpoint {argv[1]};
	std::string language {argv[2]};

	std::unique_ptr<Server> server = Server::create(serverEndpoint);
	server->init();

	std::cout << "Connected server " << *server << std::endl;

	// Connect to the responder app.
	std::string appName = std::string{"publisher-"} + language;
	std::unique_ptr<App> publisherApp = server->connect(appName);

	// Start the publisher app if it is not running.
	if (!publisherApp) {
		publisherApp = server->start(appName);
	}

	std::cout << "Application " << *publisherApp << " has state " << toString(publisherApp->getState()) << std::endl;

	// Create a subscriber.
	std::unique_ptr<coms::Subscriber> subscriber = coms::Subscriber::create(*publisherApp, "the-publisher");

	subscriber->init();

	std::cout << "Created subscriber " << *subscriber << std::endl;

	// Receive messages as long as the subscriber has not been canceled.
	while (true) {
		std::optional<std::string> message = subscriber->receive();
		if (!message.has_value()) {
			// Subscriber is canceled.
			break;
		}
		std::string value = message.value();
		std::cout << "Received " << value << std::endl;
	}

	// Stop the responder app and wait for its termination.
	publisherApp->stop();
	state::Value state = publisherApp->waitFor();

	std::cout << "App publisher finished with state " << toString(state) << std::endl;

	std::cout << "Finished the application" << std::endl;

	This::terminate();

	return 0;
}
