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

	int numberOfTimes = 1;

	if (argc > 2) {
		numberOfTimes = stoi(argv[1]);
	}

	This::init(argc, argv);

	int options = 0;
	bool useProxy = false;
	string endpoint = "tcp://localhost:11000";
	if (argc > 3) {
		useProxy = (string(argv[2]) == "true");
	}
	if (useProxy) {
		options |= option::USE_PROXY;
		endpoint = "tcp://localhost:12000";
	}

	unique_ptr<Server> server = Server::create(endpoint, options);
	server->init();

	// Loop the number of times.
	for (int i = 0; i < numberOfTimes; ++i) {

		// Test with waitFor.
		{
			// Start the application.
			unique_ptr<App> app = server->start("errorcpp");

			state::Value state = app->waitFor();

			cout << "Finished the application " << *app << " with state " << toString(state) << " and code " << app->getExitCode() << endl;
		}

		// Test with getLastState.
		{
			// Start the application.
			unique_ptr<App> app = server->start("errorcpp");

			// Check the state. When exiting the application will have terminated.
			while (app->getLastState() != state::FAILURE) {
				this_thread::sleep_for(chrono::milliseconds(100));
				cout << "...checking application state" << endl;
			}

			state::Value state = app->waitFor();

			cout << "Finished the application " << *app << " with state " << toString(state) << " and code " << app->getExitCode() << endl;
		}

		// Test with getState.
		{
			// Start the application.
			unique_ptr<App> app = server->start("errorcpp");

			// Check the state. When exiting the application will have terminated.
			while (app->getState() != state::NIL) {
				this_thread::sleep_for(chrono::milliseconds(100));
				cout << "...checking application state" << endl;
			}

			state::Value state = app->waitFor();

			cout << "Finished the application " << *app << " with state " << toString(state) << " and code " << app->getExitCode() << endl;
		}
	}

	This::terminate();

	return 0;
}
