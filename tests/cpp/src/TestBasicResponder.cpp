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
		cout << "Responder application is " + applicationName << endl;

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

		cout << "Requester ready ? " << requester->isReady() << endl;
		requester->init();
		cout << "Requester ready ? " << requester->isReady() << endl;

		// Try a second call.
		requester->init();

		cout << "Created requester " << *requester << endl;

		if (!requester) {
			cout << "Requester error" << endl;
			return -1;
		}

		cout << "Application " << *responderApplication << " has state " << toString(responderApplication->getState()) << endl;

		// Send a simple message.
		requester->send("request");

		optional<string> response = requester->receive();
		cout << "Response is " << response.value() << endl;

		response = requester->receive();
		cout << "Response 2 is " << response.value() << endl;

		// Send a two-parts message.
		requester->sendTwoParts("first", "second");

		response = requester->receive();
		cout << "Response is " << response.value() << endl;

		// Send a simple message but do not receive the response immediately.
		requester->send("request");

		cout << "Wait so that the responder has replied" << endl;
		this_thread::sleep_for(chrono::seconds(1));

		response = requester->receive();
		cout << "Response is " << response.value() << endl;

		// Send a new simple message.
		requester->send("request after timeout");
		requester->setTimeout(200);

		response = requester->receive();

		if (response.has_value()) {
			cout << "Response is " << response.value() << endl;
		}
		else {
			if (requester->hasTimedout()) {
				cout << "Timeout" << endl;
			}
			else {
				cout << "No response" << endl;
			}
		}

		// The requester needs to resync after a timeout.
		// If the server does not respond within the configured timeout, an error occurs.
		requester->send("request after timeout");
		if (requester->hasTimedout()) {
			cout << "Timeout while resyncing" << endl;
		}

		cout << "Wait so that the server is able to respond" << endl;
		this_thread::sleep_for(chrono::seconds(1));

		// Resend the request.
		requester->send("request after timeout");
		if (!requester->hasTimedout()) {
			cout << "No timeout while sending" << endl;
		}

		response = requester->receive();
		cout << "Response is " << response.value() << endl;

		// Cancel the requester.
		requester->cancel();
		requester->send("request after cancel");
		response = requester->receive();

		if (response.has_value()) {
			cout << "Response is " << response.value() << endl;
		}
		else {
			if (requester->hasTimedout()) {
				cout << "Timeout" << endl;
			}
			else if (requester->isCanceled()) {
				cout << "Canceled" << endl;
			}
		}

		// Re-init the requester has no effect.
		requester->init();
		requester->send("2nd request after cancel");
		response = requester->receive();

		if (response.has_value()) {
			cout << "Response is " << response.value() << endl;
		}
		else {
			if (requester->hasTimedout()) {
				cout << "Timeout" << endl;
			}
			else if (requester->isCanceled()) {
				cout << "Canceled" << endl;
			}
		}

		// Wait for the end of the application.
		state::Value state = responderApplication->waitFor();

		cout << "Responder application terminated with state " << toString(state) << endl;
		cout << "Finished the application" << endl;
	}

	return 0;
}
