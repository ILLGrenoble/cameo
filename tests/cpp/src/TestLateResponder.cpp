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
			cout << "Responder application is " + applicationName << endl;

			if (argc > 3) {
				numberOfTimes = stoi(argv[2]);
			}
		}
		else {
			cerr << "Arguments: [application name] <number of times>" << endl;
			return -1;
		}

		bool useProxy = false;
		string endpoint = "tcp://localhost:11000";
		if (argc > 4) {
			useProxy = (string(argv[3]) == "true");
		}
		if (useProxy) {
			endpoint = "tcp://localhost:11000:12000";
		}

		unique_ptr<Server> server = Server::create(endpoint);
		server->init();

		// Loop the number of times.
		for (int i = 0; i < numberOfTimes; ++i) {

			// Args.
			vector<string> args{(useProxy ? "true" : "false")};

			// Start the application.
			unique_ptr<App> responderApplication = server->start(applicationName, args);

			cout << "Started application " << *responderApplication << " with state " << toString(responderApplication->getState()) << endl;

			// Create a subscriber to the application applicationName
			unique_ptr<coms::Requester> requester = coms::Requester::create(*responderApplication, "responder");
			requester->setCheckApp(true);

			requester->init();
			cout << "Requester ready ? " << requester->isReady() << endl;

			// Send a simple message.
			optional<string> response = requester->request("request");
			cout << "Response is " << response.value() << endl;

			// Wait for the end of the application.
			state::Value state = responderApplication->waitFor();

			cout << "Responder application terminated with state " << toString(state) << endl;
			cout << "Finished the application" << endl;
		}
	}

	This::terminate();

	return 0;
}
