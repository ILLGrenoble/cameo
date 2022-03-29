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
#include <iostream>
#include <string>
#include <vector>
#include <sstream>

using namespace std;
using namespace cameo;

int main(int, char *[]) {

	cout << "Create server" << endl;

	Server server("tcp://localhost:11000", 0, false);

	cout << "Testing connection" << endl;

	if (server.isAvailable()) {
		cout << "Server available" << endl;
	}

	cout << "Configs" << endl;

	vector<Configuration> configs = server.getApplicationConfigurations();

	for (auto c : configs) {
		cout << c << endl;
	}

	unique_ptr<Instance> instance = server.start("simplecpp");

	if (!instance->exists()) {
		cout << "App does not exist" << endl;
	}

	State state = instance->waitFor();

	cout << "Terminated simple with state " << toString(state) << endl;

	return 0;
}
