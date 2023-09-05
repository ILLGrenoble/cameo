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

using namespace cameo;

int main(int argc, char *argv[]) {
		
	This::init(argc, argv);

	std::string serverEndpoint;
	if (argc > 2) {
		serverEndpoint = argv[1];
	}

	std::unique_ptr<Server> server;

	if (serverEndpoint == "") {
		server = Server::create(This::getServer().getEndpoint());
	}
	else {
		server = Server::create(serverEndpoint);
	}

	server->init();

	if (This::isAvailable() && server->isAvailable()) {
		std::cout << "Connected server " << *server << std::endl;
	}

	// Connect to the server.
	std::unique_ptr<App> publisherApp = server->connect("publisher");

	std::cout << "Application " << *publisherApp << " has state " << toString(publisherApp->getState()) << std::endl;

	// Create a requester.
	std::unique_ptr<coms::Subscriber> subscriber = coms::Subscriber::create(*publisherApp, "the-publisher");

	auto start = std::chrono::steady_clock::now();
	subscriber->init();
	auto end = std::chrono::steady_clock::now();

	std::cout << "Created subscriber " << *subscriber << " after " << std::chrono::duration_cast<std::chrono::milliseconds>(end - start).count() << " ms" << std::endl;

	// Receive data.
	while (true) {
		std::optional<std::string> message = subscriber->receive();
		if (!message.has_value()) {
			break;
		}
		std::string value = message.value();
		std::cout << "Received " << value << std::endl;

		json::Object object;
		if (json::parse(object, value)) {
			std::cout << "\tmessage : " << object["message"].GetString() << std::endl;
			std::cout << "\tvalue : " << object["value"].GetInt() << std::endl;
		}
	}

	std::cout << "Finished the application" << std::endl;

	return 0;
}
