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

	int options = 0;
	bool useProxy = false;
	string endpoint = "tcp://localhost:11000";
	if (argc > 3) {
		useProxy = (string(argv[2]) == "true");
	}
	if (useProxy) {
		options |= USE_PROXY;
		endpoint = "tcp://localhost:10000";
	}

	unique_ptr<Server> server = Server::create(endpoint, options);
	server->init();

	// Loop the number of times.
	for (int i = 0; i < numberOfTimes; ++i) {

		// Test with waitFor.
		{
			// Start the application.
			unique_ptr<App> app = server->start("errorcpp");

			State state = app->waitFor();

			cout << "Finished the application " << *app << " with state " << toString(state) << " and code " << app->getExitCode() << endl;
		}

		// Test with getLastState.
		{
			// Start the application.
			unique_ptr<App> app = server->start("errorcpp");

			// Check the state. When exiting the application will have terminated.
			while (app->getLastState() != FAILURE) {
				this_thread::sleep_for(chrono::milliseconds(100));
				cout << "...checking application state" << endl;
			}

			State state = app->waitFor();

			cout << "Finished the application " << *app << " with state " << toString(state) << " and code " << app->getExitCode() << endl;
		}

		// Test with getState.
		{
			// Start the application.
			unique_ptr<App> app = server->start("errorcpp");

			// Check the state. When exiting the application will have terminated.
			while (app->getState() != NIL) {
				this_thread::sleep_for(chrono::milliseconds(100));
				cout << "...checking application state" << endl;
			}

			State state = app->waitFor();

			cout << "Finished the application " << *app << " with state " << toString(state) << " and code " << app->getExitCode() << endl;
		}
	}

	return 0;
}
