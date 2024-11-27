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
#include <sstream>

using namespace std;
using namespace cameo;

int main(int argc, char *argv[]) {

	This::init(argc, argv);

	int numberOfSubscribers = 1;

	if (argc > 2) {
		istringstream is(argv[1]);
		is >> numberOfSubscribers;
	}

	cout << "Number of subscribers is " << numberOfSubscribers << endl;

	unique_ptr<coms::Publisher> publisher;

	try {
		cout << "Creating publisher and waiting for " << numberOfSubscribers << " subscriber(s)..." << endl;

		publisher = coms::Publisher::create("publisher");
		publisher->setWaitForSubscribers(numberOfSubscribers);
		publisher->init();
	}
	catch (const InitException& e) {
		cout << "Publisher error" << endl;
		return -1;
	}

	This::setRunning();

	cout << "Synchronized with " << numberOfSubscribers << " subscriber(s)" << endl;

	// Sending data.
	for (int i = 0; i < 10; ++i) {
		publisher->send("message " + to_string(i));
	}

	// Cause the program to fail. Otherwise the destructor of the publisher will send a stream end.
	throw 1;
}