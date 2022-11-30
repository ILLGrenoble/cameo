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

	bool useProxy = false;
	if (argc > 2) {
		useProxy = (string(argv[1]) == "true");
	}

	std::unique_ptr<ServerAndApp> starter = This::connectToStarter(0, useProxy);

	cout << "Connected to starter" << endl;

	// Create a requester.
	unique_ptr<coms::Requester> requester = coms::Requester::create(starter->getApp(), "an unknown responder");
	requester->setTimeout(500);

	try {
		requester->init();
	}
	catch (const std::exception& e) {
		cout << e.what() << endl;
	}

	cout << "Finished the application" << endl;

	return 0;
}

