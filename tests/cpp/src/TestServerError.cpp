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

	unique_ptr<Server> server = Server::create("tcp://localhost:12345", false);
	server->setTimeout(1000);

	try {
		server->init();
	}
	catch (const std::exception& e) {
		cerr << e.what() << endl;
	}

	server->setTimeout(1000);

	try {
		server->init();
	}
	catch (const std::exception& e) {
		cerr << e.what() << endl;
	}

	return 0;
}
