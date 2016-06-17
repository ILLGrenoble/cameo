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
#include <boost/shared_ptr.hpp>

using namespace std;
using namespace boost;
using namespace cameo;

struct StopData {
	bool stopping;

	StopData() : stopping(false) {}
};

struct Stop {

	shared_ptr<StopData> data;

	Stop(shared_ptr<StopData> sharedData) : data(sharedData) {}

	void operator()() {
		cout << "stop requested" << endl;
		data->stopping = true;
	}
};

int main(int argc, char *argv[]) {

	application::This::init(argc, argv);

	// New block to ensure cameo objects are terminated before the application.
	{
		if (application::This::isAvailable()) {
			cout << "connected" << endl;
		}

		application::This::setRunning();

		// Define an object StopData that is shared with the handler.
		shared_ptr<StopData> data(new StopData());
		application::This::handleStop(Stop(data));

		int i = 0;
		while (!data->stopping) {
			cout << "waiting " << i << "..." << endl;
			usleep(100000);
			i++;
		}

		application::This::setResult("2189");

		cout << "finished the application" << endl;
	}

	application::This::terminate();

	return 0;
}
