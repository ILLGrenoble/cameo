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
	string buffer = "{x:5}";

	try {
		string valueString = This::getCom().getKeyValue(key);
	}
	catch (const UndefinedKeyException& e) {
		cout << "Key is undefined: " << e.what() << endl;
	}

	This::getCom().storeKeyValue(key, buffer);

	try {
		This::getCom().storeKeyValue(key, buffer);
	}
	catch (const KeyAlreadyExistsException& e) {
		cout << "Key already exists: " << e.what() << endl;
	}

	try {
		string valueString = This::getCom().getKeyValue(key);
		cout << "Value is " << valueString << endl;

		This::getCom().removeKey(key);
	}
	catch (const UndefinedKeyException& e) {
	}

	try {
		This::getCom().getKeyValue(key);
	}
	catch (const UndefinedKeyException& e) {
		cout << "Cannot remove key : " << e.what() << endl;
	}

	cout << "Finished the application" << endl;

	This::terminate();

	return 0;
}
