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

	return 0;
}

