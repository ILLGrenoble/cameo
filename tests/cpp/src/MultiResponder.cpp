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

	unique_ptr<coms::multi::ResponderRouter> router;

	try {
		cout << "Creating router" << endl;

		router = coms::multi::ResponderRouter::create("responder");
		router->init();
	}
	catch (const InitException& e) {
		cout << "Responder error" << endl;
		return -1;
	}

	cout << "Created router" << endl;

	This::setRunning();

	unique_ptr<coms::multi::Responder> responder;

	std::thread td([&] {

		cout << "Creating responder" << endl;

		responder = coms::multi::Responder::create(*router);
		responder->init();

		cout << "Created responder" << endl;

		// Receive first request.
		unique_ptr<coms::multi::Request> request = responder->receive();
		cout << "Received request " << *request << endl;

		request->reply("1st response");

		// Receive second request.
		request = responder->receive();

		cout << "Received request " << request->get() << " " << request->getSecondPart() << endl;
		request->reply("2nd response");

		router->cancel();
	});

	router->run();

	td.join();

	cout << "Finished the application" << endl;

	return 0;
}
