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

	std::vector<std::string> endpoints = {"tc://badprotocol:7000", "tcp:/ferrazpc.ill.fr:7000", "tcp://ferrazpc.ill.fr", "tcp://ferrazpc.ill.fr:7000"};

	for (const auto& e : endpoints) {

		try {
			unique_ptr<Server> server = Server::create(e);
			server->setTimeout(1000);
			server->init();
		}
		catch (InitException const & e) {
			cout << "Init exception: " << e.what() << endl;
		}
		catch (ConnectionTimeout const & e) {
			cout << "Unreachable server: " << e.what() << endl;
		}
		catch (RemoteException const & e) {
			cout << "Error: " << e.what() << endl;
		}
	}

	try {
		unique_ptr<Server> server = Server::create("tcp://localhost:9999");
		server->setTimeout(1000);
		server->init();

		cout << "Server created" << endl;
		if (server->isAvailable(1000)) {
			cout << "Server available" << endl;
		}
		else {
			cout << "Server not available" << endl;
		}
	}
	catch (InitException const & e) {
		cout << "Init exception: " << e.what() << endl;
	}
	catch (ConnectionTimeout const & e) {
		cout << "Unreachable server: " << e.what() << endl;
	}
	catch (RemoteException const & e) {
		cout << "Error: " << e.what() << endl;
	}

	return 0;
}