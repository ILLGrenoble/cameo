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

	if (This::isAvailable()) {
		std::cout << "Connected" << std::endl;
	}

	This::handleStop([] {
		This::cancelAll();
	});

	std::unique_ptr<coms::basic::Responder> responder;

	try {
		responder = coms::basic::Responder::create("the-responder");
		responder->init();
		std::cout << "Created responder " << *responder << std::endl;
	}
	catch (const coms::ResponderCreationException& e) {
		std::cout << "Responder error" << std::endl;
		return -1;
	}

	This::setRunning();

	// Loop on the requests.
	while (true) {
		
		// Receive the simple request.
		std::unique_ptr<coms::basic::Request> request = responder->receive();
		if (!request) {
			std::cout << "Responder is canceled" << std::endl;
			break;
		}

		std::cout << "Received request " << request->get() << std::endl;

		// Reply.
		request->reply("done");
	}

	std::cout << "Finished the application" << std::endl;

	return 0;
}
