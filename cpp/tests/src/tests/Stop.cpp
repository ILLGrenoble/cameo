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

struct StopState {
	bool stopping;

	StopState() : stopping(false) {}
};

struct Stop {

	shared_ptr<StopState> data;

	Stop(shared_ptr<StopState> sharedData) : data(sharedData) {}

	void operator()() {
		cout << "Stop handler executed" << endl;
		data->stopping = true;
	}
};

int main(int argc, char *argv[]) {

	application::This::init(argc, argv);

	// New block to ensure cameo objects are terminated before the application.
	{
		application::This::setRunning();

		// Define an object StopState that is shared with the handler.
		shared_ptr<StopState> state(new StopState());
		application::This::handleStop(Stop(state));

		int i = 0;
		while (!state->stopping) {
			cout << "Waiting " << i << "..." << endl;
			this_thread::sleep_for(chrono::milliseconds(100));
			i++;
		}

		application::This::setResult("2189");

		cout << "Finished the application" << endl;
	}

	return 0;
}
