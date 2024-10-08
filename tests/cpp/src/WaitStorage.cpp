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

	return 0;
}

