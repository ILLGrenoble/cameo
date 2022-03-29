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

	bool useProxy = false;
	if (argc > 2) {
		useProxy = (string(argv[1]) == "true");
	}

	unique_ptr<coms::basic::Responder> responder;

	try {
		cout << "Creating responder" << endl;

		responder = coms::basic::Responder::create("responder");
	}
	catch (const coms::ResponderCreationException& e) {
		cout << "Responder error" << endl;
		return -1;
	}

	cout << "Created responder" << endl;

	This::setRunning();

	// Receive first request.
	unique_ptr<coms::basic::Request> request = responder->receive();
	cout << "Received request " << *request << endl;

	request->reply("1st response");

	// Receive second request.
	request = responder->receive();

	cout << "Received request " << request->getBinary() << " " << request->getSecondBinaryPart() << endl;
	request->reply("2nd response");

	// Receive third request.
	request = responder->receive();
	cout << "Received request " << request->get() << endl;

	// Reply.
	request->reply("3rd response");
	cout << "Replied 3rd response" << endl;

	// Receive request.
	request = responder->receive();
	cout << "received request " << *request << endl;

	cout << "Wait so that the requester has timed out" << endl;
	this_thread::sleep_for(chrono::seconds(1));

	request->reply("4th response");


	// Receive request.
	request = responder->receive();
	cout << "received request " << *request << endl;
	request->reply("5th response");


	ServerAndInstance requester = request->connectToRequester(0, useProxy);
	cout << "Requester " << *requester.instance << endl;

	cout << "Finished the application" << endl;

	return 0;
}
