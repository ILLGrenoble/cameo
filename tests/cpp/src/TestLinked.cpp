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

	int numberOfTimes = 1;

	if (argc > 2) {
		numberOfTimes = stoi(argv[1]);
	}

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

		{
			unique_ptr<App> app = server->start("linkedcpp");
			state::Value state = app->waitFor(state::RUNNING);
			unique_ptr<App> stopApp = server->connect("stopcpp");
			state = stopApp->waitFor(state::RUNNING);

			app->kill();
			app->waitFor();

			// The stop app must be killed automatically.
			state = stopApp->waitFor();

			cout << "First application stop finished with state " << toString(state) << endl;
		}

		{
			unique_ptr<App> app = server->start("linkedcpp");
			state::Value state = app->waitFor(state::RUNNING);
			unique_ptr<App> stopApp = server->connect("stopcpp");

			app->kill();
			app->waitFor();

			// The stop app must be killed automatically.
			state = stopApp->waitFor();

			cout << "Second application stop finished with state " << toString(state) << endl;
		}
	}

	This::terminate();

	return 0;
}
