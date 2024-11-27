/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

#include <iostream>
#include <string>
#include <vector>
#include <sstream>
#include <cameo/api/cameo.h>

using namespace std;
using namespace cameo;

int main(int argc, char *argv[]) {

	int numberOfTimes = 1;

	if (argc > 2) {
		numberOfTimes = stoi(argv[1]);
	}

	This::init(argc, argv);

	Server& server = This::getServer();

	int port = This::getCom().requestPort();
	cout << "Received port " << port << endl;

	// Loop the number of times.
	for (int i = 0; i < numberOfTimes; ++i) {

		// Start the application.
		unique_ptr<App> app = server.start("comportcpp");

		app->waitFor();

		cout << "Finished the application " << *app << endl;
	}

	port = This::getCom().requestPort();
	cout << "Received port " << port << " that must be greater than first port" << endl;

	return 0;
}