/*
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under the EUPL, Version 1.1 only (the "License");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */


#include <cameo/api/cameo.h>
#include <iostream>

using namespace std;
using namespace cameo;

int main(int argc, char *argv[]) {

	This::init(argc, argv);

	string applicationName;

	int numberOfTimes = 1;

	if (argc > 2) {
		applicationName = argv[1];
		cout << "Publisher application is " + applicationName << endl;

		if (argc > 3) {
			numberOfTimes = stoi(argv[2]);
		}
	}
	else {
		cerr << "Arguments: [application name]" << endl;
		return -1;
	}

	bool useProxy = false;
	string endpoint = "tcp://localhost:11000";
	if (argc > 4) {
		useProxy = (string(argv[3]) == "true");
	}
	if (useProxy) {
		endpoint = "tcp://localhost:10000";
	}

	Server server(endpoint, 0, useProxy);
	// Loop the number of times.
	for (int i = 0; i < numberOfTimes; ++i) {

		// Start the application.
		unique_ptr<Instance> publisherApplication = server.start(applicationName);

		cout << "Started application " << *publisherApplication << endl;

		// Create a subscriber to the application
		unique_ptr<coms::Subscriber> subscriber = coms::Subscriber::create(*publisherApplication, "publisher");

		cout << "Created subscriber " << *subscriber << endl;

		if (subscriber.get() == 0) {
			cout << "Subscriber error" << endl;
			return -1;
		}

		This::setRunning();

		// Receiving data.
		while (true) {
			optional<string> data = subscriber->receive();
			if (!data.has_value()) {
				break;
			}
			cout << "Received " << data.value() << endl;
		}

		cout << "Finished stream" << endl;

		State state = publisherApplication->waitFor();

		cout << "Publisher application terminated with state " << toString(state) << endl;
	}

	return 0;
}
