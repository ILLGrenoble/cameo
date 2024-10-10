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

using namespace std;
using namespace cameo;

int main(int argc, char *argv[]) {

	This::init(argc, argv);

	unique_ptr<coms::basic::Responder> responder;

	try {
		cout << "Creating responder" << endl;

		responder = coms::basic::Responder::create("responder");
		responder->init();
	}
	catch (const InitException& e) {
		cout << "Responder error" << endl;
		return -1;
	}

	cout << "Created responder " << *responder << endl;

	This::setRunning();

	// Receive one request.
	unique_ptr<coms::basic::Request> request = responder->receive();
	cout << "Received request " << *request << endl;

	unique_ptr<ServerAndApp> serverAndApp = request->connectToRequester();

	cout << "Connected to server " << serverAndApp->getServer() << endl;

	request->reply("OK");

	optional<string> result = serverAndApp->getApp().getResult();

	if (result.has_value()) {
		cout << "Got result " << result.value() << endl;
	}

	cout << "Finished the application" << endl;

	return 0;
}
