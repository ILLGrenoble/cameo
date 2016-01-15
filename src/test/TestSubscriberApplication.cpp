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
		string applicationName;

		if (argc > 2) {
			applicationName = argv[1];

			cout << "publisher application is " + applicationName << endl;

		} else {
			cerr << "arguments: [application name]" << endl;
			return -1;
		}

		Server& server = application::This::getServer();

		if (application::This::isAvailable() && server.isAvailable()) {
			cout << "connected" << endl;
		}

		auto_ptr<application::Instance> publisherApplication = server.connect(applicationName);
		if (!publisherApplication->exists()) {
			cout << "subscriber error" << endl;
			return -1;
		}

		// create a subscriber to the application applicationName
		auto_ptr<application::Subscriber> subscriber = application::Subscriber::create(*publisherApplication, "publisher");

		if (subscriber.get() == 0) {
			cout << "subscriber error" << endl;
			return -1;
		}

		cout << "synchronized with 1 publisher" << endl;

		application::This::setRunning();

		// receiving data

		string data;
		while (subscriber->receive(data)) {
			cout << "received " << data << endl;
		}

		cout << "finished stream" << endl;
		cout << "finished the application" << endl;
	}

	application::This::terminate();

	return 0;
}
