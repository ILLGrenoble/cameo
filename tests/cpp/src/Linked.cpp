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

	// Define a stop handler.
	This::handleStop([&] {
		cout << "Stop handler executed" << endl;
	});

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

	// Start the application.
	unique_ptr<App> app = server->start("stopcpp");

	This::setRunning();

	// Loop, the app will be killed.
	while (true) {
		this_thread::sleep_for(chrono::milliseconds(100));
	}

	This::terminate();

	return 0;
}
