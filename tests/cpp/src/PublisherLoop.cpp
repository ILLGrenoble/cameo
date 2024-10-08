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

	unique_ptr<coms::Publisher> publisher;

	try {
		cout << "Creating publisher and waiting for 1 subscriber..." << endl;

		publisher = coms::Publisher::create("publisher");
		publisher->setWaitForSubscribers(1);
		publisher->init();
	}
	catch (const InitException& e) {
		cout << "Publisher error" << endl;
		return -1;
	}

	cout << "Synchronized with the subscriber" << endl;

	This::setRunning();

	// sending data
	while (true) {
		publisher->send("hello");
		this_thread::sleep_for(chrono::milliseconds(100));
	}

	cout << "Finished the application" << endl;

	return 0;
}
