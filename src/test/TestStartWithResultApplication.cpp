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
#include <unistd.h>
#include <string>
#include <vector>
#include <sstream>
#include "../cameo/cameo.h"

using namespace std;
using namespace cameo;

int main(int argc, char *argv[]) {

	application::This::init(argc, argv);

	// The start function must be called into a block to ensure the destructor of Instance is called before This::terminate()
	{
		Server& server = application::This::getServer();

		if (application::This::isAvailable() && server.isAvailable()) {
			cout << "connected" << endl;
		}

		auto_ptr<application::Instance> resultApplication = server.start("rescpp");

		string result;
		if (resultApplication->getResult(result)) {
			cout << "result application returned " << result << endl;

		} else {
			cout << "result application has no result" << endl;
		}

		cout << "finished the application" << endl;
	}

	application::This::terminate();

	return 0;
}