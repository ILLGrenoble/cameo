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

std::string serializeToJSON(const std::string& message, int i) {

	rapidjson::StringBuffer buffer;
	rapidjson::Writer<rapidjson::StringBuffer> writer(buffer);

	writer.StartObject();

	writer.Key("message");
	writer.String("message");

	writer.Key("value");
	writer.Int(i);

	writer.EndObject();

	return buffer.GetString();
}

int main(int argc, char *argv[]) {

	application::This::init(argc, argv);

	// New block to ensure cameo objects are terminated before the application.
	{
		int numberOfSubscribers = 0;
		if (argc > 2) {
			numberOfSubscribers = stoi(argv[1]);
		}

		if (application::This::isAvailable()) {
			cout << "Connected" << endl;
		}

		unique_ptr<coms::Publisher> publisher;

		try {
			publisher = coms::Publisher::create("the-publisher", numberOfSubscribers);
			cout << "Created publisher " << *publisher << endl;

			publisher->waitForSubscribers();

		} catch (const coms::PublisherCreationException& e) {
			cout << "Publisher error" << endl;
			return -1;
		}

		application::This::setRunning();

		cout << "Synchronized with " << numberOfSubscribers << " subscriber(s)" << endl;
		
		// Loop on the events.
		int i = 0;
		while (true) {

			// Send a message.
			publisher->send(serializeToJSON("a message", i));
			i++;
			
			// Sleep for 1s.
			this_thread::sleep_for(chrono::seconds(1));
		}

		cout << "Finished the application" << endl;
	}

	return 0;
}
