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

	application::This::init(argc, argv);

	string applicationName;

	int numberOfTimes = 1;

	if (argc > 2) {
		applicationName = argv[1];
		cout << "Publisher application is " + applicationName << endl;

		if (argc > 3) {
			numberOfTimes = stoi(argv[2]);
		}

	} else {
		cerr << "Arguments: [application name]" << endl;
		return -1;
	}

	//Server& server = application::This::getServer();
	//Server server("tcp://gamma36:10000", 0, true);
	Server server("tcp://gamma36:11000", 0, false);

	// Loop the number of times.
	for (int i = 0; i < numberOfTimes; ++i) {

		// Start the application.
		unique_ptr<application::Instance> publisherApplication = server.start(applicationName);

		cout << "Started application " << *publisherApplication << endl;

		// Create a subscriber to the application
		unique_ptr<coms::Subscriber> subscriber = coms::Subscriber::create(*publisherApplication, "publisher");

		cout << "Created subscriber " << *subscriber << endl;

		if (subscriber.get() == 0) {
			cout << "Subscriber error" << endl;
			return -1;
		}

		application::This::setRunning();

		// Receiving data.
		while (true) {
			optional<string> data = subscriber->receive();
			if (!data.has_value()) {
				break;
			}
			cout << "Received " << data.value() << endl;
		}

		cout << "Finished stream" << endl;

		application::State state = publisherApplication->waitFor();

		cout << "Publisher application terminated with state " << application::toString(state) << endl;
	}

	return 0;
}
