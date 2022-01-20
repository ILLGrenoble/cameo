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
#include <cameo/api/cameo.h>

using namespace std;
using namespace cameo;

int main(int argc, char *argv[]) {

	int numberOfTimes = 1;

	if (argc > 2) {
		numberOfTimes = stoi(argv[1]);
	}

	application::This::init(argc, argv);

	Server& server = application::This::getServer();

	int port = application::This::getCom().requestPort();
	cout << "Received port " << port << endl;

	// Loop the number of times.
	for (int i = 0; i < numberOfTimes; ++i) {

		// Start the application.
		unique_ptr<application::Instance> app = server.start("comportcpp");

		app->waitFor();

		cout << "Finished the application " << *app << endl;
	}

	port = application::This::getCom().requestPort();
	cout << "Received port " << port << " that must be greater than first port" << endl;

	return 0;
}
