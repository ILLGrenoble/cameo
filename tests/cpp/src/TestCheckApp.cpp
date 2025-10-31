/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
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

	int options = 0;
	bool useProxy = false;
	string endpoint = "tcp://localhost:11000";
	if (argc > 3) {
		useProxy = (string(argv[2]) == "true");
	}
	if (useProxy) {
		options |= option::USE_PROXY;
		endpoint = "tcp://localhost:12000";
	}

	unique_ptr<Server> server = Server::create(endpoint, options);
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
				if (!appFinished[j] && apps[j]->getLastState() == state::SUCCESS) {
					counter++;
					appFinished[j] = true;
					cout << "App " << j << " finished" << endl;
				}
			}
		}

		cout << "Finished loop" << endl << endl;
	}

	This::terminate();

	return 0;
}
