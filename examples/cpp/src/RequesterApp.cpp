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

	// Initialize cameo.
	This::init(argc, argv);

	// Parameters: responder endpoint, language, message, number of times.
	if (argc < 6) {
		std::cout << "Parameters: <responder endpoint> <language> <message> <number of times>" << std::endl;
		return EXIT_FAILURE;
	}

	std::string responderEndpoint {argv[1]};
	std::string language {argv[2]};
	std::string message {argv[3]};
	int N {std::stoi(argv[4])};

	// Initialize the cameo server.
	std::unique_ptr<Server> server = Server::create(responderEndpoint);
	server->init();

	std::cout << "Connected server " << *server << std::endl;

	// Connect to the responder app.
	std::string appName = std::string{"responder-"} + language;
	std::unique_ptr<App> responderApp = server->connect(appName);

	// Start the responder app if it is not running.
	if (!responderApp) {
		responderApp = server->start(appName);
	}

	std::cout << "App " << *responderApp << " has state " << toString(responderApp->getState()) << std::endl;

	// Create a requester.
	std::unique_ptr<coms::Requester> requester = coms::Requester::create(*responderApp, "the-responder");
	requester->init();

	std::cout << "Created requester " << *requester << std::endl;

	if (!requester) {
		std::cout << "requester error" << std::endl;
		return EXIT_FAILURE;
	}

	for (int i = 0; i < N; ++i) {

		// Send a simple message as string.
		requester->send(message + "-" + std::to_string(i));

		// Receive the response.
		std::optional<std::string> response = requester->receive();

		std::cout << "Response is " << response.value() << std::endl;
	}

	// Stop the responder app and wait for its termination.
	responderApp->stop();
	state::Value state = responderApp->waitFor();

	std::cout << "App responder finished with state " << toString(state) << std::endl;

	return EXIT_SUCCESS;
}
