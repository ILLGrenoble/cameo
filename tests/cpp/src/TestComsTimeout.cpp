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

	This::setRunning();

	// Start the application.
	unique_ptr<App> app = server->start("comstimeoutcpp");

	// Wait for running to synchronize with the beginning of the requester init.
	app->waitFor(cameo::state::RUNNING);

	// Wait for 250ms which is half the requester timeout.
	std::this_thread::sleep_for(std::chrono::milliseconds(250));

	// Store a key to generate events in the keyvalue getter.
	This::getCom().storeKeyValue("test", "value");
	This::getCom().removeKey("test");

	// Create a publisher that will never init.
	unique_ptr<coms::Publisher> publisher = coms::Publisher::create("pub");
	publisher->setWaitForSubscribers(2);

	std::thread initThread([&] {
		publisher->init();
	});

	std::this_thread::sleep_for(std::chrono::milliseconds(200));
	publisher->cancel();
	initThread.join();

	cout << "Canceled publisher" << endl;

	app->waitFor();

	cout << "Finished the application" << endl;

	return 0;
}