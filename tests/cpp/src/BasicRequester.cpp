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

	int options = 0;
	bool useProxy = false;
	if (argc > 2) {
		useProxy = (string(argv[1]) == "true");
	}
	if (useProxy) {
		options |= option::USE_PROXY;
	}

	std::unique_ptr<ServerAndApp> starter = This::connectToStarter(options);

	cout << "Connected to starter" << endl;

	// Create a requester.
	unique_ptr<coms::Requester> requester = coms::Requester::create(starter->getApp(), "responder");
	requester->init();

	cout << "Created requester" << endl;

	This::setRunning();

	// Send 10 requests.
	int R = 10;
	for (int i = 0; i < R; ++i) {
		// Send and wait for the result.
		requester->send("test");

		std::optional<std::string> response = requester->receive();

		if (response.has_value()) {
			cout << "Received " << response.value() << endl;
		}
	}

	cout << "Finished the application" << endl;

	return 0;
}

