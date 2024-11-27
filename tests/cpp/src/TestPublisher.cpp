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

using namespace std;
using namespace cameo;

int main(int argc, char *argv[]) {

	This::init(argc, argv);

	string applicationName;

	int numberOfTimes = 1;

	if (argc > 2) {
		applicationName = argv[1];
		cout << "Publisher application is " + applicationName << endl;

		if (argc > 3) {
			numberOfTimes = stoi(argv[2]);
		}
	}
	else {
		cerr << "Arguments: [application name]" << endl;
		return -1;
	}

	int options = 0;
	bool useProxy = false;
	string endpoint = "tcp://localhost:11000";
	if (argc > 4) {
		useProxy = (string(argv[3]) == "true");
	}
	if (useProxy) {
		options |= option::USE_PROXY;
		endpoint = "tcp://localhost:12000";
	}

	unique_ptr<Server> server = Server::create(endpoint, options);
	server->init();

	// Loop the number of times.
	for (int i = 0; i < numberOfTimes; ++i) {

		// Start the application.
		unique_ptr<App> publisherApplication = server->start(applicationName);

		cout << "Started application " << *publisherApplication << endl;

		// Create a subscriber to the application
		unique_ptr<coms::Subscriber> subscriber = coms::Subscriber::create(*publisherApplication, "publisher");

		cout << "Subscriber ready ? " << subscriber->isReady() << endl;
		subscriber->init();
		cout << "Subscriber ready ? " << subscriber->isReady() << endl;

		// Try a second init.
		subscriber->init();

		cout << "Created subscriber " << *subscriber << endl;

		if (!subscriber) {
			cout << "Subscriber error" << endl;
			return -1;
		}

		This::setRunning();

		// Receiving data.
		while (true) {
			optional<string> data = subscriber->receive();
			if (!data.has_value()) {
				break;
			}
			cout << "Received " << data.value() << endl;
		}

		cout << "Finished stream" << endl;

		state::Value state = publisherApplication->waitFor();

		cout << "Publisher application terminated with state " << toString(state) << endl;
	}

	return 0;
}