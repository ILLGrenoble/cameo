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
#include "../cameo/cameo.h"

using namespace std;
using namespace cameo;

struct StateHandler {
	void operator()(application::State state) {
		cout << "received state " << application::toString(state) << endl;
	}
};

int main(int argc, char *argv[]) {

	application::This::init(argc, argv);

	// New block to ensure cameo objects are terminated before the application.
	{
		Server& server = application::This::getServer();

		if (application::This::isAvailable() && server.isAvailable()) {
			cout << "connected" << endl;
		}

		auto_ptr<application::Instance> stopApplication = server.start("stopcpp");

		cout << "waiting 1s..." << endl;
		sleep(1);

		cout << "stopping application " << stopApplication->getNameId() << endl;
		stopApplication->stop();

		stopApplication->waitFor(StateHandler());

		string result;
		if (stopApplication->getResult(result)) {
			cout << "stop application returned " << result << endl;

		} else {
			cout << "stop application has no result" << endl;
		}

		cout << "finished the application" << endl;
	}

	application::This::terminate();

	return 0;
}
