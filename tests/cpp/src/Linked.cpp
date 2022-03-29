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

	// Define a stop handler.
	This::handleStop([&] {
		cout << "Stop handler executed" << endl;
	});

	bool useProxy = false;
	string endpoint = "tcp://localhost:11000";
	if (argc > 3) {
		useProxy = (string(argv[2]) == "true");
	}
	if (useProxy) {
		endpoint = "tcp://localhost:10000";
	}

	Server server(endpoint, 0, useProxy);

	// Start the application.
	unique_ptr<App> app = server.start("stopcpp");

	This::setRunning();

	// Loop, the app will be killed.
	while (true) {
		this_thread::sleep_for(chrono::milliseconds(100));
	}

	return 0;
}
