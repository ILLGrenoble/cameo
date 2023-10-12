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

	This::init(argc, argv);

	int options = 0;
	bool useProxy = false;
	string endpoint = "tcp://localhost:11000";
	if (argc > 2) {
		useProxy = (string(argv[1]) == "true");
	}
	if (useProxy) {
		options |= USE_PROXY;
		endpoint = "tcp://localhost:10000";
	}

	unique_ptr<Server> server = Server::create(endpoint, options);
	server->init();

	// Test the cancelAll function.
	{
		cout << "Starting stopcpp for cancelAll" << endl;

		unique_ptr<App> stopApplication = server->start("stopcpp");

		// Start thread.
		thread cancelThread([] {
			this_thread::sleep_for(chrono::seconds(1));
			This::cancelAll();
		});

		State state = stopApplication->waitFor();

		cout << "End of waitFor with state " << toString(state) << endl;

		stopApplication->stop();
		state = stopApplication->waitFor();

		cout << "End of stopcpp with state " << toString(state) << endl;

		cancelThread.join();
	}

	// Test the cancel function.
	{
		cout << "Starting stopcpp for cancelWaitFor" << endl;

		// Use a shared_ptr to use it in the thread and the main thread.
		shared_ptr<App> stopApplication(server->start("stopcpp"));

		// Start thread.
		thread cancelThread([&] {
			this_thread::sleep_for(chrono::seconds(1));
			stopApplication->cancel();
		});

		State state = stopApplication->waitFor();

		cout << "End of waitFor with state " << toString(state) << endl;

		stopApplication->stop();
		state = stopApplication->waitFor();

		cout << "End of stopcpp with state " << toString(state) << endl;

		cancelThread.join();
	}

	// Test cancel
	{
		cout << "Creating publisher and waiting for 1 subscriber..." << endl;

		// Use a shared_ptr to use it in the thread and the main thread.
		shared_ptr<coms::Publisher> publisher(coms::Publisher::create("publisher"));
		publisher->setWaitForSubscribers(1);

		// Start thread.
		thread cancelThread([&] {
			this_thread::sleep_for(chrono::seconds(1));
			publisher->cancel();
		});

		publisher->init();

		cancelThread.join();

		cout << "Synchronization with subscriber " << !publisher->isCanceled() << endl;
	}

	// Test the killing of the application.
	{
		cout << "Starting publisherloopcpp for killing" << endl;

		// Use a shared_ptr to use it in the thread and the main thread.
		shared_ptr<App> pubLoopApplication(server->start("publisherloopcpp"));

		// Start thread.
		thread killThread([&] {
			this_thread::sleep_for(chrono::seconds(1));
			pubLoopApplication->kill();
		});

		// Create a subscriber checking the app.
		unique_ptr<coms::Subscriber> subscriber = coms::Subscriber::create(*pubLoopApplication, "publisher");
		subscriber->setCheckApp(true);
		subscriber->init();

		// Receiving data.
		while (true) {
			optional<string> data = subscriber->receive();
			if (!data.has_value()) {
				break;
			}
			cout << "Received " << data.value() << endl;
		}

		cout << "Subscriber end of stream " << subscriber->hasEnded() << endl;

		State state = pubLoopApplication->waitFor();

		cout << "End of publisherloopcpp with state " << toString(state) << endl;

		killThread.join();
	}

	// Test the Subscriber init.
	{
		cout << "Creating subscriber for being canceled" << endl;

		// Get this app.
		unique_ptr<App> thisApp = server->connect(This::getName());

		// Create a requester.
		unique_ptr<coms::Subscriber> subscriber = coms::Subscriber::create(*thisApp, "an unknown publisher");

		// Start thread.
		thread cancelThread([&] {
			this_thread::sleep_for(chrono::seconds(1));
			subscriber->cancel();
		});

		subscriber->init();

		cout << "Subscriber ready and canceled ? " << subscriber->isReady() << " " << subscriber->isCanceled() << endl;

		cancelThread.join();
	}

	// Test the canceling of a subscriber.
	{
		cout << "Starting publisherloopcpp for testing cancel of a subscriber" << endl;

		// Use a shared_ptr to use it in the thread and the main thread.
		shared_ptr<App> pubLoopApplication(server->start("publisherloopcpp"));

		// Create a subscriber.
		unique_ptr<coms::Subscriber> subscriber = coms::Subscriber::create(*pubLoopApplication, "publisher");
		subscriber->init();

		// Start thread.
		thread cancelThread([] {
			this_thread::sleep_for(chrono::seconds(1));
			This::cancelAll();
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

		State state = pubLoopApplication->waitFor();

		cout << "End of publisherloopcpp with state " << toString(state) << endl;

		cancelThread.join();
		killThread.join();
	}

	// Test the basic Responder.
	{
		cout << "Creating basic responder and waiting for requests" << endl;

		// Create a responder.
		unique_ptr<coms::basic::Responder> responder = coms::basic::Responder::create("responder");
		responder->init();

		// Start thread.
		thread cancelThread([&] {
			this_thread::sleep_for(chrono::seconds(1));
			responder->cancel();
		});

		cout << "Wait for requests" << endl;

		unique_ptr<coms::basic::Request> request = responder->receive();

		if (request) {
			cerr << "Responder error: receive should return null" << endl;
		}

		cancelThread.join();
	}

	// Test the Requester init.
	{
		cout << "Creating requester for being canceled" << endl;

		// Get this app.
		unique_ptr<App> thisApp = server->connect(This::getName());

		// Create a requester.
		unique_ptr<coms::Requester> requester = coms::Requester::create(*thisApp, "an unknown responder");

		// Start thread.
		thread cancelThread([&] {
			this_thread::sleep_for(chrono::seconds(1));
			requester->cancel();
		});

		requester->init();

		cout << "Requester ready and canceled ? " << requester->isReady() << " " << requester->isCanceled() << endl;

		cancelThread.join();
	}

	// Test the basic Requester.
	{
		cout << "Creating basic responder and requester" << endl;

		// Create a responder.
		unique_ptr<coms::basic::Responder> responder = coms::basic::Responder::create("responder");
		responder->init();

		// Start thread.
		thread responderThread([&] {
			while (!responder->isCanceled()) {
				responder->receive();
			}
		});

		// Get this app.
		unique_ptr<App> thisApp = server->connect(This::getName());

		// Create a requester.
		unique_ptr<coms::Requester> requester = coms::Requester::create(*thisApp, "responder");
		requester->init();

		// Start thread.
		thread cancelThread([&] {
			this_thread::sleep_for(chrono::seconds(1));
			requester->cancel();
			responder->cancel();
		});

		cout << "Sending request" << endl;

		requester->send("request");

		cout << "Receiving response" << endl;

		requester->receive();

		if (requester->isCanceled()) {
			cout << "Requester is canceled" << endl;
		}
		else {
			cout << "Requester is not canceled" << endl;
		}

		cancelThread.join();
		responderThread.join();
	}

	// Test the multi responder.
	{
		cout << "Creating multi responder" << endl;

		unique_ptr<coms::multi::ResponderRouter> router = coms::multi::ResponderRouter::create("responder");
		router->init();

		unique_ptr<coms::multi::Responder> responder = coms::multi::Responder::create(*router);
		responder->init();

		std::thread routerThread([&] {
			router->run();
		});

		std::thread responderThread([&] {
			responder->receive();
		});

		responder->cancel();
		router->cancel();

		if (responder->isCanceled() && router->isCanceled()) {
			cout << "Router and responder are canceled" << endl;
		}
		else {
			cout << "Router and responder are not canceled" << endl;
		}

		responderThread.join();
		routerThread.join();
	}

	cout << "Finished the application" << endl;

	return 0;
}
