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
#include <sstream>

using namespace std;
using namespace cameo;

int main(int argc, char *argv[]) {

	This::init(argc, argv);

	bool syncSubscribers = false;
	int numberOfSubscribers = 1;

	if (argc > 2) {
		syncSubscribers = (string(argv[1]) == "true");
	}

	if (argc > 3) {
		istringstream is(argv[2]);
		is >> numberOfSubscribers;
	}

	cout << "Number of subscribers is " << numberOfSubscribers << endl;
	cout << "Sync subscribers is " << syncSubscribers << endl;

	unique_ptr<coms::Publisher> publisher;

	try {
		cout << "Creating publisher and waiting for " << numberOfSubscribers << " subscriber(s)..." << endl;

		publisher = coms::Publisher::create("publisher");
		publisher->setSyncSubscribers(syncSubscribers);
		publisher->setWaitForSubscribers(numberOfSubscribers);
		publisher->init();
	}
	catch (const InitException& e) {
		cout << "Publisher error" << endl;
		return -1;
	}

	This::setRunning();

	cout << "Synchronized with " << numberOfSubscribers << " subscriber(s)" << endl;

	// Sending data.
	for (int i = 0; i < 100; ++i) {
		publisher->send("message " + to_string(i));
	}

	publisher->sendEnd();

	cout << "Finished the application" << endl;

	return 0;
}
