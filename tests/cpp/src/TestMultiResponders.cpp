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
		options |= USE_PROXY;
		endpoint = "tcp://localhost:12000";
	}

	unique_ptr<Server> server = Server::create(endpoint, options);
	server->init();

	// Args.
	vector<string> args{argv[2]};

	// Start the application.
	unique_ptr<App> responderApplication = server->start(applicationName, args);

	cout << "Started application " << *responderApplication << endl;

	constexpr int N = 5;

	// Instantiate the requesters sequentially with the same application.
	vector<unique_ptr<coms::Requester>> requesters;
	requesters.reserve(N);

	for (int t = 0; t < N; ++t) {

		cout << "Creating requester..." << endl;

		// Create a requester to the application applicationName
		unique_ptr<coms::Requester> requester = coms::Requester::create(*responderApplication, "responder");
		requester->init();
		requesters.push_back(std::move(requester));

		cout << "Created requester" << endl;
	}

	std::thread tds[N];

	for (int t = 0; t < N; ++t) {

		tds[t] = std::thread([=,&responderApplication,&requesters] {

			// Loop the number of times * N.
			for (int i = 0; i < numberOfTimes; ++i) {

				// Send a request.
				requesters[t]->send(std::to_string(i));
				optional<string> response = requesters[t]->receive();

				cout << t << " receives " << response.value() << endl;
			}
		});
	}

	for (int t = 0; t < N; ++t) {
		tds[t].join();
	}

	// Stop the responder application.
	responderApplication->stop();

	// Wait for the end of the application.
	State state = responderApplication->waitFor();

	cout << "Responder application terminated with state " << toString(state) << endl;
	cout << "Finished the application" << endl;

	return 0;
}
