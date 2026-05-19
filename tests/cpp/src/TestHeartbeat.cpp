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
	{
		string applicationName;

		int numberOfTimes = 1;

		if (argc > 2) {
			applicationName = argv[1];
			cout << "Hearbeat application is " + applicationName << endl;

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

		This::heartbeat(1, 1);

		// Args.
		vector<string> args{(useProxy ? "true" : "false")};

		// Start the application.
		unique_ptr<App> heartbeatApplication = server->start(applicationName, args);

		// Create a subscriber to the application applicationName
		unique_ptr<coms::Requester> requester = coms::Requester::create(*heartbeatApplication, "responder");
		requester->init();

		cout << "Requester ready ? " << requester->isReady() << endl;

		unique_ptr<coms::Publisher> publisher = coms::Publisher::create("publisher");
		publisher->init();

		cout << "Publisher ready ? " << publisher->isReady() << endl;

		unique_ptr<coms::Publisher> publisherNotPinged = coms::Publisher::create("publisher-not-pinged");
		publisherNotPinged->setPinged(false);
		publisherNotPinged->init();

		cout << "Publisher ready ? " << publisherNotPinged->isReady() << endl;

		int N = 10;
		for (int i = 0; i < N; i++) {
			cout << (i + 1) << " / " << N << endl;
			std::this_thread::sleep_for(std::chrono::seconds(1));
		}

		cout << "Waiting for the application" << endl;

		heartbeatApplication->waitFor();

		cout << "Application terminated" << endl;
	}

	cout << "Finishing application" << endl;

	This::terminate();

	return 0;
}
