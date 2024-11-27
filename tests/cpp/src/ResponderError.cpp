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

	bool useProxy = false;
	if (argc > 2) {
		useProxy = (string(argv[1]) == "true");
	}

	unique_ptr<coms::basic::Responder> responder;

	try {
		cout << "Creating responder" << endl;

		responder = coms::basic::Responder::create("responder");
		responder->init();
	}
	catch (const InitException& e) {
		cout << "Responder error" << endl;
		return -1;
	}

	cout << "Created responder " << *responder << endl;

	This::setRunning();

	// Receive a request.
	unique_ptr<coms::basic::Request> request = responder->receive();
	cout << "Received request " << *request << endl;

	request->reply("response");

	cout << "Finished the application" << endl;

	return 123;
}