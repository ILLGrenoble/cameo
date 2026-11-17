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
	{
		int options = 0;
		bool useProxy = false;
		if (argc > 2) {
			useProxy = (string(argv[1]) == "true");
		}
		if (useProxy) {
			options |= option::USE_PROXY;
		}

		cout << "Creating responder after 1s" << endl;

		this_thread::sleep_for(std::chrono::seconds(1));

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

		// Receive first request.
		unique_ptr<coms::basic::Request> request = responder->receive();
		cout << "Received request " << *request << endl;

		request->reply("The response");

		cout << "Finished the application" << endl;
	}

	This::terminate();

	return 0;
}
