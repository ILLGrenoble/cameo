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
#include <sstream>

using namespace std;
using namespace cameo;

int main(int argc, char *argv[]) {
		
	application::This::init(argc, argv);

	// The request message is the second argument.
	string requestMessage;
	if (argc > 2) {
		requestMessage = argv[1];
	}

	int N = 1;
	if (argc > 3) {
		istringstream is(argv[2]);
		is >> N;
	}

	string serverEndpoint;
	if (argc > 4) {
		serverEndpoint = argv[3];
	}

	unique_ptr<Server> server;

	if (serverEndpoint == "") {
		server.reset(new Server(application::This::getServer().getEndpoint()));

	} else {
		server.reset(new Server(serverEndpoint));
	}


	if (application::This::isAvailable() && server->isAvailable()) {
		cout << "Connected server " << *server << endl;
	}

	// Connect to the server.
	unique_ptr<application::Instance> responderServer = server->connect("responder");

	cout << "Application " << *responderServer << " has state " << application::toString(responderServer->now()) << endl;

	// Create a requester.
	unique_ptr<coms::legacy::Requester> requester = coms::legacy::Requester::create(*responderServer, "the-responder");

	cout << "Created requester " << *requester << endl;

	if (requester.get() == 0) {
		cout << "requester error" << endl;
		return -1;
	}

	for (int i = 0; i < N; ++i) {

		// Send a simple message as string.
		requester->send(requestMessage + "-" + to_string(i));

		// Receive the response.
		optional<string> response = requester->receive();

		cout << "Response is " << response.value() << endl;
	}

	cout << "Finished the application" << endl;

	return 0;
}
