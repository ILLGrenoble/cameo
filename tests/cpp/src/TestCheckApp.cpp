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

	This::init(argc, argv);

	bool useProxy = false;
	string endpoint = "tcp://localhost:11000";
	if (argc > 3) {
		useProxy = (string(argv[2]) == "true");
	}
	if (useProxy) {
		endpoint = "tcp://localhost:10000";
	}

	unique_ptr<Server> server = Server::create(endpoint, useProxy);
	server->init();

	// loop the number of times.
	for (int i = 0; i < numberOfTimes; ++i) {

		// start the applications.
		vector<unique_ptr<App>> apps;

		int N = 100;

		for (int j = 0; j < N; ++j) {
			apps.push_back(server->start("veryfastcpp"));
		}

		int counter = 0;
		vector<bool> appFinished(N, false);

		while (counter < N) {

			for (int j = 0; j < N; ++j) {
				if (!appFinished[j] && apps[j]->getLastState() == SUCCESS) {
					counter++;
					appFinished[j] = true;
					cout << "App " << j << " finished" << endl;
				}
			}
		}

		cout << "Finished loop" << endl << endl;
	}

	return 0;
}
