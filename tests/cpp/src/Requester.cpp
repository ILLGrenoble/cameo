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

	unique_ptr<application::Instance> starter = application::This::connectToStarter();

	cout << "Connected to started" << endl;

	// Create a requester.
	unique_ptr<coms::legacy::Requester> requester = coms::legacy::Requester::create(*starter, "responder");

	cout << "Created requester" << endl;

	application::This::setRunning();

	// Send 10 requests.
	int R = 10;
	for (int i = 0; i < R; ++i) {
		// Send and wait for the result.
		requester->send("test");

		std::optional<std::string> response = requester->receive();

		if (response.has_value()) {
			cout << "Received " << response.value() << endl;
		}

		this_thread::sleep_for(chrono::milliseconds(100));
	}

	cout << "Finished the application" << endl;

	return 0;
}

