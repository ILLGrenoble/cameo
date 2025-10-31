/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

#include <iostream>
#include <string>
#include <vector>
#include <sstream>
#include <cameo/api/cameo.h>

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

	// Define the shared key.
	string key = "eu.ill.cameo.test.testkey";

	// loop the number of times.
	for (int i = 0; i < numberOfTimes; ++i) {

		// start the application.
		unique_ptr<App> app = server->start("waitstoragecpp");

		// Define a KeyValue.
		KeyValue keyValue(key);

		// waitFor blocks.
		app->waitFor(keyValue);
		cout << "storage event " << keyValue.getStatus() << " " << keyValue.getValue() << endl;

		// Get the key value.
		cout << "key value " << app->getCom().getKeyValue(key) << endl;

		// waitFor blocks.
		app->waitFor(keyValue);
		cout << "storage event " << keyValue.getStatus() << " " << keyValue.getValue() << endl;

		state::Value state = app->waitFor();

		cout << "finished the application " << *app << " with state " << toString(state) << endl;
	}

	This::terminate();

	return 0;
}
