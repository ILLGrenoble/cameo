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
#include <thread>
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

	std::thread td([&] {
		// Get this app.
		unique_ptr<App> thisApp = server->connect(This::getName());

		// Create a subscriber to the application
		unique_ptr<coms::Subscriber> subscriber = coms::Subscriber::create(*thisApp, "publisher");
		subscriber->init();

		cout << "Created subscriber " << *subscriber << endl;

		optional<string> data = subscriber->receive();

		if (data.has_value()) {
			cout << "Received " << data.value() << endl;
		}

		subscriber->setTimeout(500);

		data = subscriber->receive();

		if (data.has_value()) {
			cout << "Received " << data.value() << endl;
		}
		else {
			cout << "Has not received data, has timedout " << subscriber->hasTimedout() << endl;
		}

		data = subscriber->receive();

		if (data.has_value()) {
			cout << "Received " << data.value() << endl;
		}
		else {
			cout << "Has not received, has timedout " << subscriber->hasTimedout() << endl;
		}

	});

	unique_ptr<coms::Publisher> publisher = coms::Publisher::create("publisher");
	publisher->setSyncSubscribers(true);
	publisher->setWaitForSubscribers(1);
	publisher->init();

	publisher->send("first message");

	this_thread::sleep_for(chrono::milliseconds(1000));

	publisher->send("message after timeout");


	cout << "Wait for subscriber termination" << endl;
	td.join();
	cout << "Subscriber terminated" << endl;

	return 0;
}