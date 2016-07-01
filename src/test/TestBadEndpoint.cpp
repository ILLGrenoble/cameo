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
#include "../cameo/cameo.h"

using namespace std;
using namespace cameo;

int main(int argc, char *argv[]) {

	try {
		Server server("tcp://bad:7000");
		server.isAvailable(1000);
	}
	catch (SocketException const & e) {
		cout << "Socket exception: " << e.what() << endl;
	}
	catch (exception const & e) {
		cout << "The server has bad endpoint" << endl;
	}

	try {
		Server server("tcp//bad:7000");
		server.isAvailable(1000);
	}
	catch (exception const &) {
		cout << "The server has bad endpoint" << endl;
	}

	return 0;
}
