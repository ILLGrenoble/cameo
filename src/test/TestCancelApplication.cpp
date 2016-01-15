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

#include <boost/bind.hpp>
#include <boost/thread.hpp>
#include <boost/shared_ptr.hpp>
#include <iostream>
#include <unistd.h>
#include <string>
#include <vector>
#include <sstream>
#include "../cameo/cameo.h"

using namespace std;
using namespace cameo;
using namespace boost;

void cancelAll() {

	sleep(1);

	application::This::cancelWaitings();
}

void cancelInstance(shared_ptr<application::Instance> instance) {

	sleep(1);

	instance->cancelWaitFor();
}

void cancelPublisher(shared_ptr<application::Publisher> publisher) {

	sleep(1);

	publisher->cancelWaitForSubscribers();
}


void killInstance(shared_ptr<application::Instance> instance) {

	sleep(1);

	instance->kill();
}

int main(int argc, char *argv[]) {

	application::This::init(argc, argv);

	// New block to ensure cameo objects are terminated before the application.
	{
		Server& server = application::This::getServer();

		// Test the cancelWaitings function
		{
			cout << "starting stopcpp for cancelWaitings" << endl;

			auto_ptr<application::Instance> stopApplication = server.start("stopcpp");

			// Start thread
			auto_ptr<thread> cancelThread(new thread(&cancelAll));

			stopApplication->waitFor();

			cout << "end of waitFor" << endl;

			stopApplication->stop();
			application::State state = stopApplication->waitFor();

			cout << "end of stopcpp with state " << application::toString(state) << endl;

			cancelThread->join();
		}

		// Test the cancel function
		{
			cout << "starting stopcpp for cancelWaitFor" << endl;

			// Use a shared_ptr to use it in the thread and the main thread.
			shared_ptr<application::Instance> stopApplication(server.start("stopcpp"));

			// Start thread
			auto_ptr<thread> cancelThread(new thread(bind(&cancelInstance, stopApplication)));

			stopApplication->waitFor();

			cout << "end of waitFor" << endl;

			stopApplication->stop();
			application::State state = stopApplication->waitFor();

			cout << "end of stopcpp with state " << application::toString(state) << endl;

			cancelThread->join();
		}

		// Test Publisher.cancelWaitForSubscribers
		{
			cout << "creating publisher and waiting for 1 subscriber..." << endl;

			// Use a shared_ptr to use it in the thread and the main thread.
			shared_ptr<application::Publisher> publisher(application::Publisher::create("publisher", 1));

			// Start thread
			auto_ptr<thread> cancelThread(new thread(bind(&cancelPublisher, publisher)));

			bool synced = publisher->waitForSubscribers();

			cancelThread->join();

			cout << "synchronization with the subscriber synced ? " << synced << endl;
		}


		// Test the killing of the application
		{
			cout << "starting publoopcpp for killing" << endl;

			// Use a shared_ptr to use it in the thread and the main thread.
			shared_ptr<application::Instance> pubLoopApplication(server.start("publoopcpp"));

			// Start thread
			auto_ptr<thread> killThread(new thread(bind(&killInstance, pubLoopApplication)));

			// Create a subscriber
			auto_ptr<application::Subscriber> subscriber = application::Subscriber::create(*pubLoopApplication, "publisher");

			// Receiving data
			string data;
			while (subscriber->receive(data)) {
				cout << "received " << data << endl;
			}

			cout << "subscriber end of stream " << subscriber->hasEnded() << endl;

			application::State state = pubLoopApplication->waitFor();

			cout << "end of publoopcpp with state " << application::toString(state) << endl;

			killThread->join();
		}
	}

	application::This::terminate();

	return 0;
}
