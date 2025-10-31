/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
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

	cout << "Created router " << *router << endl;

	This::setRunning();

	unique_ptr<coms::multi::Responder> responder;

	std::thread td([&] {

		cout << "Creating responder" << endl;

		responder = coms::multi::Responder::create(*router);
		responder->init();

		cout << "Created responder " << *responder << endl;

		// Receive first request.
		unique_ptr<coms::multi::Request> request = responder->receive();
		cout << "Received request " << *request << endl;

		request->reply("1st response");
		request->reply("1st response (bis)");

		// Receive second request.
		request = responder->receive();

		cout << "Received request " << request->get() << " " << request->getSecondPart() << endl;
		request->reply("2nd response");

		router->cancel();
	});

	router->run();

	td.join();

	cout << "Finished the application" << endl;

	This::terminate();

	return 0;
}
