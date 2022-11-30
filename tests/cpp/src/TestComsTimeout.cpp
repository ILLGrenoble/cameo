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

	bool useProxy = false;
	string endpoint = "tcp://localhost:11000";
	if (argc > 3) {
		useProxy = (string(argv[2]) == "true");
	}
	if (useProxy) {
		endpoint = "tcp://localhost:10000";
	}

	unique_ptr<Server> server = Server::create(endpoint, useProxy);
	server->init();

	This::setRunning();

	// Start the application.
	unique_ptr<App> app = server->start("comstimeoutcpp");

	// Wait for running to synchronize with the beginning of the requester init.
	app->waitFor(cameo::RUNNING);

	// Wait for 250ms which is half the requester timeout.
	std::this_thread::sleep_for(std::chrono::milliseconds(250));

	// Store a key to generate event in the keyvalue getter.
	This::getCom().storeKeyValue("test", "value");
	This::getCom().removeKey("test");

	unique_ptr<coms::Publisher> publisher = coms::Publisher::create("pub", 2);

	std::thread initThread([&] {
		publisher->init();
	});

	std::this_thread::sleep_for(std::chrono::milliseconds(200));
	publisher->cancel();
	initThread.join();

	cout << "Canceled publisher" << endl;


	app->waitFor();

	cout << "Application " << *app << " finished" << endl;




	cout << "Finished the application" << endl;

	return 0;
}
