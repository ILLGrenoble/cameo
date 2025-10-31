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

using namespace std;
using namespace cameo;

int main(int argc, char *argv[]) {

	This::init(argc, argv);

	string key = "eu.ill.cameo.test.testkey";

	try {
		// Sleep 100ms.
		this_thread::sleep_for(chrono::milliseconds(100));
		This::getCom().storeKeyValue(key, "{x:12}");

		// Sleep 100ms.
		this_thread::sleep_for(chrono::milliseconds(100));
		This::getCom().removeKey(key);
	}
	catch (const UndefinedKeyException& e) {
	}

	cout << "Finished the application" << endl;

	This::terminate();

	return 0;
}
