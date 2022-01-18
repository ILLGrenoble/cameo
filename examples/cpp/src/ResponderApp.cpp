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

	application::This::init(argc, argv);

	if (application::This::isAvailable()) {
		cout << "Connected" << endl;
	}

	application::This::handleStop([] {
		application::This::cancelWaitings();
	});

	unique_ptr<coms::Responder> responder;

	try {
		responder = coms::Responder::create("the-responder");
		cout << "Created responder " << *responder << endl;

	} catch (const coms::ResponderCreationException& e) {
		cout << "Responder error" << endl;
		return -1;
	}

	application::This::setRunning();

	// Loop on the requests.
	while (true) {
		
		// Receive the simple request.
		unique_ptr<coms::Request> request = responder->receive();
		if (!request) {
			cout << "Responder is canceled" << endl;
			break;
		}

		cout << "Received request " << request->get() << endl;

		// Reply.
		request->reply("done");
	}

	cout << "Finished the application" << endl;

	return 0;
}
