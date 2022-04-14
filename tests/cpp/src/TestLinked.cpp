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

	int numberOfTimes = 1;

	if (argc > 2) {
		numberOfTimes = stoi(argv[1]);
	}

	bool useProxy = false;
	string endpoint = "tcp://localhost:11000";
	if (argc > 3) {
		useProxy = (string(argv[2]) == "true");
	}
	if (useProxy) {
		endpoint = "tcp://localhost:10000";
	}

	unique_ptr<Server> server = Server::create(endpoint, 0, useProxy);
	server->init();

	// Loop the number of times.
	for (int i = 0; i < numberOfTimes; ++i) {

		{
			unique_ptr<App> app = server->start("linkedcpp");
			State state = app->waitFor(RUNNING);
			unique_ptr<App> stopApp = server->connect("stopcpp");
			state = stopApp->waitFor(RUNNING);

			app->kill();
			app->waitFor();

			// The stop app must be killed automatically.
			state = stopApp->waitFor();

			cout << "First application stop finished with state " << toString(state) << endl;
		}

		{
			unique_ptr<App> app = server->start("linkedcpp");
			State state = app->waitFor(RUNNING);
			unique_ptr<App> stopApp = server->connect("stopcpp");

			app->kill();
			app->waitFor();

			// The stop app must be killed automatically.
			state = stopApp->waitFor();

			cout << "Second application stop finished with state " << toString(state) << endl;
		}
	}

	return 0;
}
