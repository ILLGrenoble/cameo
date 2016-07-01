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
#include <boost/shared_ptr.hpp>

using namespace std;
using namespace boost;
using namespace cameo;

struct StopData {
	bool stop;

	StopData() : stop(false) {}
};

struct Handler {

	shared_ptr<StopData> data;

	Handler(shared_ptr<StopData> sharedData) : data(sharedData) {}

	void operator()(bool available) {
		if (available) {
			cout << "server is available" << endl;
		}
		else {
			cout << "server is not available" << endl;
			data->stop = true;
		}
	}
};

int main(int argc, char *argv[]) {

	application::This::init(argc, argv);

	// The start function must be called into a block to ensure the destructor of Instance is called before This::terminate()
	{
		Server server("tcp://localhost:9000");
		server.setTimeout(1000);

		shared_ptr<StopData> data(new StopData());
		auto_ptr<ConnectionChecker> ConnectionChecker = server.createConnectionChecker(Handler(data), 1000);

		while (!data->stop) {
			sleep(1);
		}
	}

	application::This::terminate();

	return 0;
}
