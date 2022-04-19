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

std::string serializeToJSON(const std::string& message, int i) {

	json::StringObject object;

	object.pushKey("message");
	object.pushValue(message);

	object.pushKey("value");
	object.pushValue(i);

	return object.dump();
}

int main(int argc, char *argv[]) {

	This::init(argc, argv);

	int numberOfSubscribers = 0;
	if (argc > 2) {
		numberOfSubscribers = std::stoi(argv[1]);
	}

	if (This::isAvailable()) {
		std::cout << "Connected" << std::endl;
	}

	std::unique_ptr<coms::Publisher> publisher;

	try {
		publisher = coms::Publisher::create("the-publisher", numberOfSubscribers);
		publisher->init();
		std::cout << "Created publisher " << *publisher << std::endl;
	}
	catch (const coms::PublisherCreationException& e) {
		std::cout << "Publisher error" << std::endl;
		return -1;
	}

	This::setRunning();

	std::cout << "Synchronized with " << numberOfSubscribers << " subscriber(s)" << std::endl;

	// Loop on the events.
	int i = 0;
	while (true) {

		// Send a message.
		publisher->send(serializeToJSON("a message", i));
		i++;
		
		// Sleep for 1s.
		std::this_thread::sleep_for(std::chrono::seconds(1));
	}

	std::cout << "Finished the application" << std::endl;

	return 0;
}
