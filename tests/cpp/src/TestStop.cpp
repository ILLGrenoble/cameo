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

struct StateHandler {
	void operator()(application::State state) {
		cout << "Received state " << application::toString(state) << endl;
	}
};

int main(int argc, char *argv[]) {

	int numberOfTimes = 1;

	if (argc > 2) {
		numberOfTimes = stoi(argv[1]);
	}

	application::This::init(argc, argv);

	bool useProxy = false;
	string endpoint = "tcp://localhost:11000";
	if (argc > 3) {
		useProxy = (string(argv[2]) == "true");
	}
	if (useProxy) {
		endpoint = "tcp://localhost:10000";
	}

	Server server(endpoint, 0, useProxy);

	// Loop the number of times.
	for (int i = 0; i < numberOfTimes; ++i) {

		unique_ptr<application::Instance> stopApplication = server.start("stopcpp");

		cout << "Waiting 0.1s..." << endl;
		this_thread::sleep_for(chrono::milliseconds(100));

		cout << "Stopping application " << stopApplication->getNameId() << endl;
		stopApplication->stop();

		optional<string> result = stopApplication->getResult();
		if (result.has_value()) {
			cout << "Stop application returned " << result.value() << endl;

		} else {
			cout << "Stop application has no result" << endl;
		}

		cout << "Finished the application " << *stopApplication << endl;
	}

	return 0;
}
