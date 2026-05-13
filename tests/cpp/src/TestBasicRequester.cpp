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
		cout << "Requester application is " + applicationName << endl;

		if (argc > 3) {
			numberOfTimes = stoi(argv[2]);
		}
	}
	else {
		cerr << "Arguments: [application name] <number of times>" << endl;
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

	This::setRunning();

	int N = 5;

	// loop the number of times.
	for (int i = 0; i < numberOfTimes; ++i) {

		AppArray apps;

		// Start the requester applications.
		for (int j = 0; j < N; ++j) {

			// Args.
			vector<string> args{(useProxy ? "true" : "false")};

			// Start the application.
			apps.push_back(server->start(applicationName, args));
			cout << "Started application " << *apps.back() << endl;
		}

		// Sleep 1s so that requester are waiting for the responder.
		this_thread::sleep_for(chrono::seconds(1));

		unique_ptr<coms::basic::Responder> responder;

		responder = coms::basic::Responder::create("responder");

		cout << "Responder ready ? " << responder->isReady() << endl;
		responder->init();
		cout << "Responder ready ? " << responder->isReady() << endl;

		// Try a second init.
		responder->init();

		cout << "Created responder " << *responder << endl;

		// Process the requests, the requester application sends 10 requests.
		for (int j = 0; j < N * 10; ++j) {

			// Receive the simple request.
			unique_ptr<coms::basic::Request> request = responder->receive();
			request->reply("done");

			cout << "Processed " << *request.get() << endl;
		}

		// Wait for the requester applications.
		for (int j = 0; j < N; ++j) {
			apps[j]->waitFor();
			cout << "Finished application " << *apps[j].get() << endl;
		}
	}

	cout << "Finished the application" << endl;

	This::terminate();

	return 0;
}
