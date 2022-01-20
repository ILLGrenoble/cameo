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

	string applicationName;

	if (argc > 2) {
		applicationName = argv[1];

		cout << "Publisher application is " + applicationName << endl;

	} else {
		cerr << "Arguments: [application name]" << endl;
		return -1;
	}

	Server& server = application::This::getServer();

	unique_ptr<application::Instance> publisherApplication = server.connect(applicationName);
	if (!publisherApplication->exists()) {
		cout << "Publisher error" << endl;
		return -1;
	}

	// Create a subscriber to the application applicationName.
	unique_ptr<coms::Subscriber> subscriber = coms::Subscriber::create(*publisherApplication, "publisher");

	if (subscriber.get() == 0) {
		cout << "Subscriber error" << endl;
		return -1;
	}

	cout << "Synchronized with 1 publisher" << endl;

	application::This::setRunning();

	// Receive data.
	while (true) {
		optional<string> data = subscriber->receive();
		if (!data.has_value()) {
			break;
		}
		cout << "Received " << data.value() << endl;
	}

	cout << "Finished the application" << endl;

	return 0;
}
