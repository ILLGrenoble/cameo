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

	// Initialize cameo.
	This::init(argc, argv);

	// Define the stop handler to properly stop.
	This::handleStop([] {});

	int numberOfSubscribers = 1;

	std::unique_ptr<coms::Publisher> publisher;

	try {
		// Create the publisher.
		publisher = coms::Publisher::create("the-publisher");
		publisher->setWaitForSubscribers(numberOfSubscribers);

		std::cout << "Created publisher " << *publisher << std::endl;

		publisher->init();
	}
	catch (const InitException& e) {
		std::cout << "Publisher error" << std::endl;
		return EXIT_FAILURE;
	}

	// Set the state.
	This::setRunning();

	std::cout << "Synchronized with " << numberOfSubscribers << " subscriber(s)" << std::endl;

	int i = 0;
	while (!This::isStopping()) {

		// Send a message.
		publisher->send(std::string{"a message "} + std::to_string(i));
		i++;
		
		// Sleep for 1s.
		std::this_thread::sleep_for(std::chrono::seconds(1));
	}

	std::cout << "Finished the application" << std::endl;

	return EXIT_SUCCESS;
}
