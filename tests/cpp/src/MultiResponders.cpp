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

	This::handleStop([] {
		// Cancel the router.
		This::cancelAll();

		cout << "Stopped" << endl;
	});

	int numberOfTimes = 1;

	if (argc > 2) {
		numberOfTimes = stoi(argv[1]);

		cout << "Number of times " << numberOfTimes << endl;
	}

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

	constexpr int N = 5;

	std::thread tds[N];

	for (int t = 0; t < N; ++t) {

		tds[t] = std::thread([=,&router] {

			cout << "Creating responder" << endl;

			unique_ptr<coms::multi::Responder> responder = coms::multi::Responder::create(*router);
			responder->init();

			cout << "Created responder " << *responder << endl;

			for (int i = 0; i < numberOfTimes; ++i) {

				unique_ptr<coms::multi::Request> request = responder->receive();

				cout << t << " received request " << *request << endl;

				request->reply(std::to_string(t) + string(" to ") + request->get());
			}
		});
	}

	router->run();

	for (int t = 0; t < N; ++t) {
		tds[t].join();
	}

	cout << "Finished the application" << endl;

	return 0;
}