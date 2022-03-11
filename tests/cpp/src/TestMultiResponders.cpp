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
		cout << "Responder application is " + applicationName << endl;

		if (argc > 3) {
			numberOfTimes = stoi(argv[2]);
		}
	}
	else {
		cerr << "Arguments: [application name] <number of times>" << endl;
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

	// Args.
	vector<string> args{argv[2]};

	// Start the application.
	unique_ptr<application::Instance> responderApplication = server.start(applicationName, args);

	cout << "Started application " << *responderApplication << endl;

	// Create a requester to the application applicationName
	unique_ptr<coms::Requester> requester = coms::Requester::create(*responderApplication, "responder");

	cout << "Created requester " << *requester << endl;

	int N = 5;

	// Loop the number of times * N.
	for (int i = 0; i < numberOfTimes * N; ++i) {

		// Send a request.
		requester->send(string("request-") + std::to_string(i));

		optional<string> response = requester->receive();
		cout << "Response is " << response.value() << endl;
	}

	// Wait for the end of the application.
	application::State state = responderApplication->waitFor();

	cout << "Responder application terminated with state " << application::toString(state) << endl;
	cout << "Finished the application" << endl;

	return 0;
}
