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
#include <atomic>
#include <iostream>

using namespace std;
using namespace cameo;

std::atomic_bool stopping(false);

int main(int argc, char *argv[]) {

	application::This::init(argc, argv);

	application::This::setRunning();

	// Define a stop handler.
	application::This::handleStop([&] {
		cout << "Stop handler executed" << endl;
		stopping.store(true);
	});

	int i = 0;
	while (!stopping.load()) {
		cout << "Waiting " << i << "..." << endl;
		this_thread::sleep_for(chrono::milliseconds(100));
		i++;
	}

	application::This::setResult("2189");

	cout << "Finished the application" << endl;

	return 0;
}
