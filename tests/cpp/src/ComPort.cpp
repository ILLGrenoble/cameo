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

	This::init(argc, argv);

	int port1 = This::getCom().requestPort();
	cout << "Received port1 " << port1 << endl;

	This::getCom().setPortUnavailable(port1);
	cout << "Set port " << port1 << " unavailable" << endl;

	int port2 = This::getCom().requestPort();
	cout << "Received port2 " << port2 << endl;

	This::getCom().releasePort(port2);
	cout << "Released port2 " << port2 << endl;

	port2 = This::getCom().requestPort();
	cout << "Received port2 " << port2 << endl;

	cout << "Finished the application" << endl;

	This::terminate();

	return 0;
}
