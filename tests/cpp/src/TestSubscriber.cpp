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
		cout << "subscriber application is " + applicationName << endl;

		if (argc > 3) {
			numberOfTimes = stoi(argv[2]);
		}

	} else {
		cerr << "Arguments: [subscriber application name] [number of subscribers]" << endl;
		return -1;
	}

	Server& server = application::This::getServer();

	// Loop the number of times.
	for (int i = 0; i < numberOfTimes; ++i) {

		// Create 5 subscribers.
		for (int j = 0; j < 5; ++j) {

			// Pass the name of the application in argument.
			vector<string> applicationArgs;
			applicationArgs.push_back(application::This::getName());

			// Start the application.
			unique_ptr<application::Instance> subscriberApplication = server.start(applicationName, applicationArgs);

			if (subscriberApplication->exists()) {
				cout << "Started application " << *subscriberApplication << endl;
			}
			else {
				cout << "Cannot start subscriber application" << endl;
			}
		}

		// Sleep for 1s to let the subscribers wait.
		this_thread::sleep_for(chrono::seconds(1));

		// The publisher is created after the applications that will wait for it.
		unique_ptr<coms::Publisher> publisher = coms::Publisher::create("publisher");

		for (int k = 0; k < 20; ++k) {

			string data = "{";
			data += to_string(k) + ", " + to_string(k * k) + "}";
			publisher->send(data);

			cout << "Sent " << data << endl;

			this_thread::sleep_for(chrono::milliseconds(100));
		}

		// The publisher must terminate so that the subscriber applications receive end of stream.
		publisher->sendEnd();

		// Wait for the end of the applications.
		application::InstanceArray instances = server.connectAll(applicationName);

		for (size_t i = 0; i < instances.size(); i++) {
			instances[i]->waitFor();
		}
	}

	cout << "Finished the application" << endl;

	return 0;
}