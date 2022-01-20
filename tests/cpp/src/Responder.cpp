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

	application::This::init(argc, argv);

	unique_ptr<coms::Responder> responder;

	try {
		cout << "Creating responder" << endl;

		responder = coms::Responder::create("responder");
	}
	catch (const coms::ResponderCreationException& e) {
		cout << "Responder error" << endl;
		return -1;
	}

	application::This::setRunning();

	// Receive first request.
	unique_ptr<coms::Request> request = responder->receive();
	cout << "Received request " << *request << endl;

	request->reply("1st response");

	// Receive second request.
	request = responder->receive();

	string data1, data2;

	parse(request->getBinary(), data1);
	parse(request->getSecondBinaryPart(), data2);
	cout << "Received request " << data1 << " " << data2 << endl;

	bool res = request->reply("2nd response");
	if (!res) {
		cout << "Error, no timeout expected" << endl;
	}

	// Receive third request without receive on the requester side.
	request = responder->receive();
	cout << "Received request " << request->get() << endl;

	// Reply with timeout
	request->setTimeout(100);
	res = request->reply("3rd response");

	if (!res) {
		cout << "Timeout with " << request->getObjectId() << endl;
	}

	// Receive request after timeout.
	request = responder->receive();
	cout << "received request " << *request << endl;

	request->reply("4th response after timeout");


	unique_ptr<application::Instance> requester = request->connectToRequester();
	cout << "Requester " << *requester << endl;

	cout << "Finished the application" << endl;

	return 0;
}
