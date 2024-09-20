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

	// Test start.
	try {
		// Start the application.
		unique_ptr<App> app = server->start("fuzz");
	}
	catch (const StartException& e) {
		cout << "Application fuzz cannot be started" << endl;
	}

	// Connect the application.
	unique_ptr<App> app = server->connect("fuzz");

	if (!app) {
		cout << "Application fuzz cannot be connected" << endl;
	}

	// Test basic responder.
	cout << "Creating basic responder" << endl;

	unique_ptr<coms::basic::Responder> basicResponder = coms::basic::Responder::create("basic-responder");
	basicResponder->init();

	cout << "Created basic responder" << endl;

	unique_ptr<coms::basic::Responder> basicResponder2 = coms::basic::Responder::create("basic-responder");

	try {
		basicResponder2->init();
	}
	catch (const InitException& e) {
		cout << "Basic responder cannot be created: " << e.what() << endl;
	}


	// Test multi responder.
	cout << "Creating multi responder" << endl;

	unique_ptr<coms::multi::ResponderRouter> multiResponder = coms::multi::ResponderRouter::create("multi-responder");
	multiResponder->init();

	cout << "Created multi responder" << endl;

	unique_ptr<coms::multi::ResponderRouter> multiResponder2 = coms::multi::ResponderRouter::create("multi-responder");

	try {
		multiResponder2->init();
	}
	catch (const InitException& e) {
		cout << "Multi responder cannot be created: " << e.what() << endl;
	}


	// Test publisher.
	cout << "Creating publisher" << endl;

	unique_ptr<coms::Publisher> publisher = coms::Publisher::create("publisher");
	publisher->init();

	cout << "Created publisher" << endl;

	unique_ptr<coms::Publisher> publisher2 = coms::Publisher::create("publisher");

	try {
		publisher2->init();
	}
	catch (const InitException& e) {
		cout << "Publisher cannot be created: " << e.what() << endl;
	}

	return 0;
}
