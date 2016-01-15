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
#include <unistd.h>
#include <string>
#include <vector>
#include <sstream>
#include "../cameo/cameo.h"

using namespace std;
using namespace cameo;

int main(int argc, char *argv[]) {

	application::This::init(argc, argv);

	// New block to ensure cameo objects are terminated before the application.
	{
		int numberOfSubscribers = 1;

		if (argc > 2) {
			istringstream is(argv[1]);
			is >> numberOfSubscribers;
		}

		cout << "number of subscribers is " << numberOfSubscribers << endl;

		if (application::This::isAvailable()) {
			cout << "connected" << endl;
		}

		auto_ptr<application::Publisher> publisher;

		try {
			cout << "creating publisher and waiting for " << numberOfSubscribers << " subscriber(s)..." << endl;

			publisher = application::Publisher::create("publisher", numberOfSubscribers);
			publisher->waitForSubscribers();

		} catch (const PublisherCreationException& e) {
			cout << "publisher error" << endl;
			application::This::terminate();
			return -1;
		}

		application::This::setRunning();

		cout << "synchronized with " << numberOfSubscribers << " subscriber(s)" << endl;

		// sending data
		publisher->send("hello");
		publisher->send("world");
		publisher->send("!");

		publisher->sendEnd();

		cout << "finished the application" << endl;
	}

	application::This::terminate();

	return 0;
}
