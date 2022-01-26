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
		cout << "Requester application is " + applicationName << endl;

		if (argc > 3) {
			numberOfTimes = stoi(argv[2]);
		}

	} else {
		cerr << "Arguments: [application name] <number of times>" << endl;
		return -1;
	}



	Server& server = application::This::getServer();

	application::This::setRunning();

	int N = 5;

	// loop the number of times.
	for (int i = 0; i < numberOfTimes; ++i) {

		application::InstanceArray apps;

		// Start the requester applications.
		for (int j = 0; j < N; ++j) {

			// Start the application.
			apps.push_back(server.start(applicationName));
			cout << "Started application " << *apps.back() << endl;
		}

		// Sleep 1s so that requester are waiting for the responder.
		this_thread::sleep_for(chrono::seconds(1));

		unique_ptr<coms::legacy::Responder> responder;

		try {
			cout << "Creating responder" << endl;

			responder = coms::legacy::Responder::create("responder");
		}
		catch (const coms::ResponderCreationException& e) {
			cout << "Responder error" << endl;
			return -1;
		}

		// Process the requests, the requester application sends 10 requests.
		for (int j = 0; j < N * 10; ++j) {

			// Receive the simple request.
			unique_ptr<coms::legacy::Request> request = responder->receive();
			request->reply("done");

			cout << "Processed " << *request.get() << endl;
		}

		// Wait for the requester applications.
		for (int j = 0; j < N; ++j) {
			apps[j]->waitFor();
			cout << "Finished application " << *apps[j].get() << endl;
		}
	}

	cout << "Finished the application" << endl;

	return 0;
}
