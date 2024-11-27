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

	This::setRunning();

	int i = 0;
	while (i < 20) {
		cout << "Printing " << i << endl;
		this_thread::sleep_for(chrono::milliseconds(100));
		i++;
	}

	cout << "Finished the application" << endl;

	return 0;
}