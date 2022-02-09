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

	string applicationName;

	int numberOfTimes = 1;

	if (argc > 2) {
		applicationName = argv[1];
		cout << "Responder application is " + applicationName << endl;

		if (argc > 3) {
			numberOfTimes = stoi(argv[2]);
		}

	} else {
		cerr << "Arguments: [application name] <number of times>" << endl;
		return -1;
	}

	Server& server = application::This::getServer();

	// Loop the number of times.
	for (int i = 0; i < numberOfTimes; ++i) {

		// Start the application.
		unique_ptr<application::Instance> responderApplication = server.start(applicationName);

		cout << "Started application " << *responderApplication << " with state " << application::toString(responderApplication->now()) << endl;

		// Create a subscriber to the application applicationName
		unique_ptr<coms::basic::Requester> requester = coms::basic::Requester::create(*responderApplication, "responder");

		cout << "Created requester " << *requester << endl;

		if (requester.get() == 0) {
			cout << "Requester error" << endl;
			return -1;
		}

		cout << "Application " << *responderApplication << " has state " << application::toString(responderApplication->now()) << endl;

		// Send a simple message.
		requester->send("request");

		optional<string> response = requester->receive();
		cout << "Response is " << response.value() << endl;

		// Send a two-parts message.
		string buffer1, buffer2;

		serialize("first", buffer1);
		serialize("second", buffer2);

		requester->sendTwoBinaryParts(buffer1, buffer2);

		response = requester->receive();
		cout << "Response is " << response.value() << endl;

		// Send a simple message but do not receive the response immediately.
		requester->send("request");

		cout << "Wait so that the responder has replied" << endl;
		this_thread::sleep_for(chrono::seconds(1));

		response = requester->receive();
		cout << "Response is " << response.value() << endl;

		// Send a new simple message.
		requester->send("request after timeout");
		requester->setTimeout(500);

		response = requester->receive();

		if (response.has_value()) {
			cout << "Response is " << response.value() << endl;
		}
		else {
			if (requester->hasTimedout()) {
				cout << "Timeout" << endl;
			}
			else {
				cout << "No response" << endl;
			}
		}

		// Send a simple message.
		requester->send("request after timeout");

		response = requester->receive();
		cout << "Response is " << response.value() << endl;

		// Wait for the end of the application.
		application::State state = responderApplication->waitFor();

		cout << "Responder application terminated with state " << application::toString(state) << endl;
		cout << "Finished the application" << endl;
	}

	return 0;
}
