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
		cout << "Responder application is " + applicationName << endl;

		if (argc > 3) {
			numberOfTimes = stoi(argv[2]);
		}
	}
	else {
		cerr << "Arguments: [application name] <number of times>" << endl;
		return -1;
	}

	int options = 0;
	bool useProxy = false;
	string endpoint = "tcp://localhost:11000";
	if (argc > 4) {
		useProxy = (string(argv[3]) == "true");
	}
	if (useProxy) {
		options |= option::USE_PROXY;
		endpoint = "tcp://localhost:12000";
	}

	unique_ptr<Server> server = Server::create(endpoint, options);
	server->init();

	// Loop the number of times.
	for (int i = 0; i < numberOfTimes; ++i) {

		// Args.
		vector<string> args{(useProxy ? "true" : "false")};

		// Start the application.
		unique_ptr<App> responderApplication = server->start(applicationName, args);

		cout << "Started application " << *responderApplication << " with state " << toString(responderApplication->getState()) << endl;

		// Create a subscriber to the application applicationName
		unique_ptr<coms::Requester> requester = coms::Requester::create(*responderApplication, "responder");
		requester->setCheckApp(true);
		requester->init();

		cout << "Created requester " << *requester << endl;

		if (!requester) {
			cout << "Requester error" << endl;
			return -1;
		}

		cout << "Application " << *responderApplication << " has state " << toString(responderApplication->getState()) << endl;

		// Send a simple message.
		requester->send("request");

		// The app is terminating at this point so that the requester won't receive anything.
		optional<string> response = requester->receive();
		cout << "Response is " << response.value() << endl;

		response = requester->receive();

		// The requester is canceled because the app is terminated.
		if (!response.has_value()) {
			cout << "No response" << endl;
		}

		if (requester->isCanceled()) {
			// The last state is FAILURE.
			cout << "Requester canceled, last responder application state " << toString(responderApplication->getLastState()) << endl;
		}

		state::Value state = responderApplication->waitFor();

		cout << "Responder application terminated with state " << toString(state) << endl;

		cout << "Finished the application" << endl;
	}

	return 0;
}
