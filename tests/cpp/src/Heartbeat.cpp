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

		unique_ptr<coms::basic::Responder> responder;

		cout << "Creating responder" << endl;

		responder = coms::basic::Responder::create("responder");
		responder->init();

		cout << "Created responder " << *responder << endl;

		This::setRunning();

		unique_ptr<coms::basic::Request> request = responder->receive();
		const std::string& requestString = request->get();
		request->reply("1");
		this_thread::sleep_for(chrono::seconds(1));
		request->reply("2");
		this_thread::sleep_for(chrono::seconds(1));
		request->reply("3");
		this_thread::sleep_for(chrono::seconds(1));

		thread cancelThread([&] {
			this_thread::sleep_for(chrono::seconds(2));
			cout << "Canceling responder" << endl;
			responder->cancel();
			cout << "Canceled responder" << endl;
		});

		request = responder->receive();

		cancelThread.join();

		cout << "Finished the application" << endl;
	}

	This::terminate();

	return 0;
}
