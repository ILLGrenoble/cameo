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

	int numberOfTimes = 1;

	if (argc > 2) {
		numberOfTimes = stoi(argv[1]);
	}

	This::init(argc, argv);

	This::handleStop([&] {
		cout << "Stop handler executed" << endl;
	});

	int options = 0;
	bool useProxy = false;
	string endpoint = "tcp://localhost:11000";
	if (argc > 3) {
		useProxy = (string(argv[2]) == "true");
	}
	if (useProxy) {
		options |= option::USE_PROXY;
		endpoint = "tcp://localhost:12000";
	}

	unique_ptr<Server> server = Server::create(endpoint, options);

	cout << "Created server " << *server << endl;

	cout << "Server ready ? " << server->isReady() << endl;
	server->init();
	cout << "Server ready ? " << server->isReady() << endl;

	// Try a second init.
	server->init();

	auto start = chrono::steady_clock::now();

	// Loop the number of times.
	for (int i = 0; i < numberOfTimes; ++i) {

		// Start the application.
		unique_ptr<App> app = server->start("simplecpp");

		State state = app->waitFor();

		cout << "Finished the application " << *app << " with state " << toString(state) << " and code " << app->getExitCode() << endl;
	}

	auto end = chrono::steady_clock::now();

	cout << "Finished the application after " << chrono::duration_cast<chrono::milliseconds>(end - start).count() << " ms" << endl;

	return 0;
}
