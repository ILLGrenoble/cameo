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
		string applicationName;

		if (argc > 2) {
			applicationName = argv[1];

			cout << "Publisher application is " + applicationName << endl;

		} else {
			cerr << "Arguments: [application name]" << endl;
			return -1;
		}

		Server& server = This::getServer();

		unique_ptr<App> publisherApplication = server.connect(applicationName);

		// Create a subscriber to the application applicationName.
		unique_ptr<coms::Subscriber> subscriber = coms::Subscriber::create(*publisherApplication, "publisher");
		subscriber->init();

		if (!subscriber) {
			cout << "Subscriber error" << endl;
			return -1;
		}

		cout << "Synchronized with 1 publisher" << endl;

		This::setRunning();

		// Receive data.
		while (true) {
			optional<tuple<string, string>> data = subscriber->receiveTwoParts();
			if (!data.has_value()) {
				break;
			}
			cout << "Received " << get<0>(data.value()) << ", " << get<1>(data.value()) << endl;
		}

		cout << "Finished the application" << endl;
	}

	This::terminate();

	return 0;
}
