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

struct StateHandler {
	void operator()(state::Value state) {
		cout << "Received state " << toString(state) << endl;
	}
};

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

		unique_ptr<App> stopApplication = server->start("stopcpp");

		cout << "Waiting 0.1s..." << endl;
		this_thread::sleep_for(chrono::milliseconds(100));

		cout << "Stopping application " << stopApplication->getNameId() << endl;
		stopApplication->stop();

		optional<string> result = stopApplication->getResult();
		if (result.has_value()) {
			cout << "Stop application returned " << result.value() << endl;

		} else {
			cout << "Stop application has no result" << endl;
		}

		cout << "Finished the application " << *stopApplication << endl;
	}

	return 0;
}