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
	if (argc > 2) {
		useProxy = (string(argv[1]) == "true");
	}
	if (useProxy) {
		options |= option::USE_PROXY;
		endpoint = "tcp://localhost:12000";
	}

	unique_ptr<Server> server = Server::create(endpoint, options);
	server->init();

	// Start the application.
	unique_ptr<App> app = server->start("streamcpp", cameo::option::OUTPUTSTREAM);

	shared_ptr<OutputStreamSocket> socket = app->getOutputStreamSocket();
	std::thread outputThread([&] {
		while (true) {
			std::optional<Output> output = socket->receive();
			if (output) {
				cout << output.value().getMessage() << endl;
			}
			else {
				return;
			}
		}
	});

	this_thread::sleep_for(chrono::seconds(1));

	cout << "Canceling output" << endl;
	socket->cancel();
	outputThread.join();

	app->waitFor();

	cout << "Finished the application" << endl;

	return 0;
}