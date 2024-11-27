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

	unique_ptr<coms::Publisher> publisher;

	try {
		cout << "Creating publisher and waiting for 1 subscriber..." << endl;

		publisher = coms::Publisher::create("publisher");
		publisher->setWaitForSubscribers(1);
		publisher->init();
	}
	catch (const InitException& e) {
		cout << "Publisher error" << endl;
		return -1;
	}

	cout << "Synchronized with the subscriber" << endl;

	This::setRunning();

	// sending data
	while (true) {
		publisher->send("hello");
		this_thread::sleep_for(chrono::milliseconds(100));
	}

	cout << "Finished the application" << endl;

	return 0;
}