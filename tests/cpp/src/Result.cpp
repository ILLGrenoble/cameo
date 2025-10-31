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

	cout << "Waiting 1s..." << endl;
	this_thread::sleep_for(chrono::seconds(1));

	This::setResult("test result");

	cout << "Finished the application" << endl;

	This::terminate();

	return 0;
}
