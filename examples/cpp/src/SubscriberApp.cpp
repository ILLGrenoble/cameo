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
#include <rapidjson/stringbuffer.h>
#include <rapidjson/writer.h>
#include <rapidjson/document.h>
#include <iostream>

using namespace std;
using namespace cameo;

int main(int argc, char *argv[]) {
		
	application::This::init(argc, argv);

	// New block to ensure cameo objects are terminated before the application.
	{
		string serverEndpoint;
		if (argc > 2) {
			serverEndpoint = argv[1];
		}

		unique_ptr<Server> server;

		if (serverEndpoint == "") {
			server.reset(new Server(application::This::getServer().getEndpoint()));

		} else {
			server.reset(new Server(serverEndpoint));
		}

		
		if (application::This::isAvailable() && server->isAvailable()) {
			cout << "Connected server " << *server << endl;
		}

		// Connect to the server.
		unique_ptr<application::Instance> publisherApp = server->connect("publisher");

		cout << "Application " << *publisherApp << " has state " << application::toString(publisherApp->now()) << endl;

		// Create a requester.
		unique_ptr<coms::Subscriber> subscriber = coms::Subscriber::create(*publisherApp, "the-publisher");

		cout << "Created subscriber " << *subscriber << endl;

		// Receive data.
		while (true) {
			optional<string> message = subscriber->receive();
			if (!message.has_value()) {
				break;
			}
			string value = message.value();
			cout << "Received " << value << endl;

			rapidjson::Document object;
			object.Parse(static_cast<const char *>(value.c_str()), value.size());

			cout << "\tmessage : " << object["message"].GetString() << endl;
			cout << "\tvalue : " << object["value"].GetInt() << endl;
		}

		cout << "Finished the application" << endl;
	}

	return 0;
}
