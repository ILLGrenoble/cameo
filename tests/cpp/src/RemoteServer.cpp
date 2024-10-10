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

	if (argc <= 1) {
		cout << "Usage: <endpoint>" << endl;
		return -1;
	}

	string endpoint {string(argv[1])};

	This::init("remoteserver", endpoint);

	const string testAppName {"testremoteservercpp"};
	unique_ptr<App> testApp = This::getServer().connect(testAppName);

	if (!testApp) {
		cout << "Cannot connect remote app " << testAppName << endl;
		return -1;
	}

	unique_ptr<coms::Requester> requester;

	try {
		cout << "Creating requester" << endl;

		requester = coms::Requester::create(*testApp, "responder");
		requester->init();
	}
	catch (const InitException& e) {
		cout << "Requester error" << endl;
		return -1;
	}

	cout << "Created requester " << *requester << endl;

	This::setRunning();

	requester->send("A message");

	optional<string> response = requester->receive();

	This::setResult("The result");

	cout << "Finished the application" << endl;

	return 0;
}
