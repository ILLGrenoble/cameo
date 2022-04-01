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

using namespace cameo;

int main(int argc, char *argv[]) {
		
	This::init(argc, argv);

	// The request message is the second argument.
	std::string requestMessage;
	if (argc > 2) {
		requestMessage = argv[1];
	}

	int N = 1;
	if (argc > 3) {
		std::istringstream is(argv[2]);
		is >> N;
	}

	std::string serverEndpoint;
	if (argc > 4) {
		serverEndpoint = argv[3];
	}

	std::unique_ptr<Server> server;

	if (serverEndpoint == "") {
		server.reset(new Server(This::getServer().getEndpoint()));
	}
	else {
		server.reset(new Server(serverEndpoint));
	}


	if (This::isAvailable() && server->isAvailable()) {
		std::cout << "Connected server " << *server << std::endl;
	}

	// Connect to the server.
	std::unique_ptr<App> responderServer = server->connect("responder");

	std::cout << "Application " << *responderServer << " has state " << toString(responderServer->getActualState()) << std::endl;

	// Create a requester.
	std::unique_ptr<coms::Requester> requester = coms::Requester::create(*responderServer, "the-responder");

	std::cout << "Created requester " << *requester << std::endl;

	if (requester.get() == 0) {
		std::cout << "requester error" << std::endl;
		return -1;
	}

	for (int i = 0; i < N; ++i) {

		// Send a simple message as string.
		requester->send(requestMessage + "-" + std::to_string(i));

		// Receive the response.
		std::optional<std::string> response = requester->receive();

		std::cout << "Response is " << response.value() << std::endl;
	}

	std::cout << "Finished the application" << std::endl;

	return 0;
}
