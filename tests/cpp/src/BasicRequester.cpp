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

	int options = 0;
	bool useProxy = false;
	if (argc > 2) {
		useProxy = (string(argv[1]) == "true");
	}
	if (useProxy) {
		options |= option::USE_PROXY;
	}

	std::unique_ptr<ServerAndApp> starter = This::connectToStarter(options);

	cout << "Connected to starter" << endl;

	// Create a requester.
	unique_ptr<coms::Requester> requester = coms::Requester::create(starter->getApp(), "responder");
	requester->init();

	cout << "Created requester" << endl;

	This::setRunning();

	// Send 10 requests.
	int R = 10;
	for (int i = 0; i < R; ++i) {
		// Send and wait for the result.
		requester->send("test");

		std::optional<std::string> response = requester->receive();

		if (response.has_value()) {
			cout << "Received " << response.value() << endl;
		}
	}

	cout << "Finished the application" << endl;

	return 0;
}