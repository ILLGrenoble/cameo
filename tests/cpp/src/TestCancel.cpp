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
#include <thread>
#include <iostream>

using namespace std;
using namespace cameo;

int main(int argc, char *argv[]) {

	application::This::init(argc, argv);

	Server& server = application::This::getServer();

	// Test the cancelWaitings function.
	{
		cout << "Starting stopcpp for cancelWaitings" << endl;

		unique_ptr<application::Instance> stopApplication = server.start("stopcpp");

		// Start thread.
		thread cancelThread([] {
			this_thread::sleep_for(chrono::seconds(1));
			application::This::cancelWaitings();
		});

		application::State state = stopApplication->waitFor();

		cout << "End of waitFor with state " << application::toString(state) << endl;

		stopApplication->stop();
		state = stopApplication->waitFor();

		cout << "End of stopcpp with state " << application::toString(state) << endl;

		cancelThread.join();
	}

	// Test the cancel function.
	{
		cout << "Starting stopcpp for cancelWaitFor" << endl;

		// Use a shared_ptr to use it in the thread and the main thread.
		shared_ptr<application::Instance> stopApplication(server.start("stopcpp"));

		// Start thread.
		thread cancelThread([&] {
			this_thread::sleep_for(chrono::seconds(1));
			stopApplication->cancelWaitFor();
		});

		application::State state = stopApplication->waitFor();

		cout << "End of waitFor with state " << application::toString(state) << endl;

		stopApplication->stop();
		state = stopApplication->waitFor();

		cout << "End of stopcpp with state " << application::toString(state) << endl;

		cancelThread.join();
	}

	// Test cancelWaitForSubscribers.
	{
		cout << "Creating publisher and waiting for 1 subscriber..." << endl;

		// Use a shared_ptr to use it in the thread and the main thread.
		shared_ptr<coms::Publisher> publisher(coms::Publisher::create("publisher", 1));

		// Start thread.
		thread cancelThread([&] {
			this_thread::sleep_for(chrono::seconds(1));
			publisher->cancelWaitForSubscribers();
		});

		bool synced = publisher->waitForSubscribers();

		cancelThread.join();

		cout << "Synchronization with the subscriber " << synced << endl;
	}

	// Test the killing of the application.
	{
		cout << "Starting publisherloopcpp for killing" << endl;

		// Use a shared_ptr to use it in the thread and the main thread.
		shared_ptr<application::Instance> pubLoopApplication(server.start("publisherloopcpp"));

		// Start thread.
		thread killThread([&] {
			this_thread::sleep_for(chrono::seconds(1));
			pubLoopApplication->kill();
		});

		// Create a subscriber.
		unique_ptr<coms::Subscriber> subscriber = coms::Subscriber::create(*pubLoopApplication, "publisher");

		// Receiving data.
		while (true) {
			optional<string> data = subscriber->receive();
			if (!data.has_value()) {
				break;
			}
			cout << "Received " << data.value() << endl;
		}

		cout << "Subscriber end of stream " << subscriber->hasEnded() << endl;

		application::State state = pubLoopApplication->waitFor();

		cout << "End of publisherloopcpp with state " << application::toString(state) << endl;

		killThread.join();
	}

	// Test the canceling of a subscriber.
	{
		cout << "Starting publisherloopcpp for testing cancel of a subscriber" << endl;

		// Use a shared_ptr to use it in the thread and the main thread.
		shared_ptr<application::Instance> pubLoopApplication(server.start("publisherloopcpp"));

		// Create a subscriber.
		unique_ptr<coms::Subscriber> subscriber = coms::Subscriber::create(*pubLoopApplication, "publisher");

		// Start thread.
		thread cancelThread([] {
			this_thread::sleep_for(chrono::seconds(1));
			application::This::cancelWaitings();
		});

		// Receiving data.
		while (true) {
			optional<string> data = subscriber->receive();
			if (!data.has_value()) {
				break;
			}
			cout << "Received " << data.value() << endl;
		}

		cout << "Subscriber end of stream " << subscriber->hasEnded() << endl;

		// Start thread.
		thread killThread([&] {
			this_thread::sleep_for(chrono::seconds(1));
			pubLoopApplication->kill();
		});

		application::State state = pubLoopApplication->waitFor();

		cout << "End of publisherloopcpp with state " << application::toString(state) << endl;

		cancelThread.join();
		killThread.join();
	}

	// Test the Responder.
	{
		cout << "Creating responder and waiting for requests" << endl;

		// Create a responder.
		unique_ptr<coms::legacy::Responder> responder = coms::legacy::Responder::create("responder");

		// Start thread.
		thread cancelThread([&] {
			this_thread::sleep_for(chrono::seconds(1));
			responder->cancel();
		});

		cout << "Wait for requests" << endl;

		unique_ptr<coms::legacy::Request> request = responder->receive();

		if (request) {
			cerr << "Responder error: receive should return null" << endl;
		}

		cancelThread.join();
	}

	cout << "Finished the application" << endl;

	return 0;
}
