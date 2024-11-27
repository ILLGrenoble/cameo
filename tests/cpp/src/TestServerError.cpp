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

	unique_ptr<Server> server = Server::create("tcp://localhost:12345");
	server->setTimeout(1000);

	try {
		server->init();
	}
	catch (const std::exception& e) {
		cerr << e.what() << endl;
	}

	server->setTimeout(1000);

	try {
		server->init();
	}
	catch (const std::exception& e) {
		cerr << e.what() << endl;
	}

	return 0;
}