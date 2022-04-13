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

#include <iostream>
#include <string>
#include <vector>
#include <sstream>
#include <cameo/api/cameo.h>

using namespace std;
using namespace cameo;

int main(int argc, char *argv[]) {

	int numberOfTimes = 1;

	if (argc > 2) {
		numberOfTimes = stoi(argv[1]);
	}

	This::init(argc, argv);

	bool useProxy = false;
	string endpoint = "tcp://localhost:11000";
	if (argc > 3) {
		useProxy = (string(argv[2]) == "true");
	}
	if (useProxy) {
		endpoint = "tcp://localhost:10000";
	}

	unique_ptr<Server> server = Server::create(endpoint, 0, useProxy);

	// Define the shared key.
	string key = "fr.ill.ics.cameo.test.testkey";

	// loop the number of times.
	for (int i = 0; i < numberOfTimes; ++i) {

		// start the application.
		unique_ptr<App> app = server->start("waitstoragecpp");

		// Define a KeyValue.
		KeyValue keyValue(key);

		// waitFor blocks.
		app->waitFor(keyValue);
		cout << "storage event " << keyValue.getStatus() << " " << keyValue.getValue() << endl;

		// Get the key value.
		cout << "key value " << app->getCom().getKeyValue(key) << endl;

		// waitFor blocks.
		app->waitFor(keyValue);
		cout << "storage event " << keyValue.getStatus() << " " << keyValue.getValue() << endl;

		State state = app->waitFor();

		cout << "finished the application " << *app << " with state " << toString(state) << endl;
	}

	return 0;
}
