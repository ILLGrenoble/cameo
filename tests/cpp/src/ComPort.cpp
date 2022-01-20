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

#include <iostream>
#include <string>
#include <vector>
#include <sstream>
#include <cameo/api/cameo.h>

using namespace std;
using namespace cameo;

int main(int argc, char *argv[]) {

	application::This::init(argc, argv);

	int port1 = application::This::getCom().requestPort();
	cout << "Received port1 " << port1 << endl;

	application::This::getCom().setPortUnavailable(port1);
	cout << "Set port " << port1 << " unavailable" << endl;

	int port2 = application::This::getCom().requestPort();
	cout << "Received port2 " << port2 << endl;

	application::This::getCom().releasePort(port2);
	cout << "Released port2 " << port2 << endl;

	port2 = application::This::getCom().requestPort();
	cout << "Received port2 " << port2 << endl;

	cout << "Finished the application" << endl;

	return 0;
}
