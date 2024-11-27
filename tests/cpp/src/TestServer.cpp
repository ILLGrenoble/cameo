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
#include <string>
#include <vector>
#include <sstream>

using namespace std;
using namespace cameo;

int main(int, char *[]) {

	cout << "Create server" << endl;

	unique_ptr<Server> server = Server::create("tcp://localhost:11000");
	server->setTimeout(1000);
	server->init();

	cout << "Testing connection" << endl;

	if (server->isAvailable()) {
		cout << "Server available" << endl;
	}

	cout << "Configs" << endl;

	vector<App::Config> configs = server->getApplicationConfigs();

	for (auto c : configs) {
		cout << c << endl;
	}

	try {
		unique_ptr<App> instance = server->start("simplecpp");
		state::Value state = instance->waitFor();

		cout << "Terminated simple with state " << toString(state) << endl;
	}
	catch (const StartException& e) {
		cout << "Cannot start application" << endl;
	}

	return 0;
}