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
#include <boost/date_time/posix_time/posix_time.hpp>

using namespace std;
using namespace cameo;

struct ConnectionHandler {
	void operator()(bool available) {
		if (!available) {
			application::This::cancelWaitings();
		}
	}
};

int main(int argc, char *argv[]) {

	application::This::init(argc, argv);

	// The start function must be called into a block to ensure the destructor of Instance is called before This::terminate()
	{
		Server server("tcp://localhost:8000");

		server.addConnectionHandler("timeout", ConnectionHandler());
		server.setTimeout(100, 100);
		sleep(1);
		//server.setTimeout(0);

		cout << "reset timeout" << endl;

		/*
		if (server.isAvailable()) {
			cout << "connected" << endl;

			auto_ptr<application::Instance> resultApplication = server.start("test");

			cout << "finished the application" << endl;
		}
		else {
			cout << "not connected" << endl;

			try {
				auto_ptr<application::Instance> resultApplication = server.start("test");
			}
			catch (ConnectionTimeout const & e) {
				cout << e.what() << endl;
			}

			cout << "started" << endl;
		}*/
	}

	application::This::terminate();

	return 0;
}
