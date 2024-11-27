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
	if (argc > 2) {
		useProxy = (string(argv[1]) == "true");
	}
	if (useProxy) {
		options |= option::USE_PROXY;
	}

	std::unique_ptr<ServerAndApp> starter = This::connectToStarter(options);

	cout << "Connected to starter" << endl;

	// Create a requester.
	unique_ptr<coms::Requester> requester = coms::Requester::create(starter->getApp(), "an unknown responder");
	requester->setTimeout(500);

	// Set running to synchronize the starter app.
	This::setRunning();

	try {
		requester->init();
	}
	catch (const std::exception& e) {
		cout << e.what() << endl;
		cout << "Requester ready ? " << requester->isReady() << endl;
	}

	// Create a subscriber.
	unique_ptr<coms::Subscriber> subscriber = coms::Subscriber::create(starter->getApp(), "an unknown publisher");
	subscriber->setTimeout(500);

	try {
		subscriber->init();
	}
	catch (const std::exception& e) {
		cout << e.what() << endl;
		cout << "Subscriber ready ? " << subscriber->isReady() << endl;
	}

	// Create a subscriber.
	subscriber = coms::Subscriber::create(starter->getApp(), "pub");
	subscriber->setTimeout(500);

	try {
		subscriber->init();
	}
	catch (const std::exception& e) {
		cout << e.what() << endl;
		cout << "Subscriber ready ? " << subscriber->isReady() << endl;
	}

	cout << "Finished the application" << endl;

	return 0;
}