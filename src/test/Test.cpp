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

using namespace std;
using namespace cameo;

int N = -1;

void testFast(Server& server) {

	server.killAllAndWaitFor("fast");

	int i = 0;
	while (N == -1 || i < N) {

		auto_ptr<application::Instance> result = server.start("fast");
		cout << "new application " << result->getNameId() << endl;

		usleep(10000);

		result->kill();
		result->waitFor();

		i++;
	}
}

void testFastAsync(Server& server) {

	server.killAllAndWaitFor("fast");

	int i = 0;
	while (N == -1 || i < N) {

		auto_ptr<application::Instance> result = server.start("fast");
		cout << "new application " << result->getNameId() << endl;

		usleep(10000);

		// replace the result to test the Instance of kill
		result->kill();
		result->waitFor();

		i++;
	}
}


void testFastRes(Server& server) {

	server.killAllAndWaitFor("fastres");

	int i = 0;
	while (N == -1 || i < N) {

		auto_ptr<application::Instance> res = server.start("fastres");
		cout << "new application " << res->getNameId() << endl;

		application::State state = res->waitFor();

		if (state == application::SUCCESS) {

			string result;
			res->getResult(result);
			istringstream buffer(result);
			int id;
			buffer >> id;
			cout << "success " << (id / 11) << " = " << res->getId() << endl;

		} else {
			cout << "error" << endl;
		}

		i++;
	}
}

void testFastResPar1(Server& server) {

	server.killAllAndWaitFor("fastres");

	const int M = 10;

	auto_ptr<application::Instance> res[M];

	// start in parallel
	for (int j = 0; j < M; j++) {

		res[j] = server.start("fastres");
		cout << "new application " << res[j]->getNameId() << endl;
	}

	// process
	for (int j = 0; j < M; j++) {
		application::State state = res[j]->waitFor();

		if (state == application::SUCCESS) {

			string result;
			res[j]->getResult(result);
			istringstream buffer(result);
			int id;
			buffer >> id;
			cout << "success " << (id / 11) << " = " << res[j]->getId() << endl;

		} else {
			cout << "error" << endl;
		}
	}
}

void testFastResPar(Server& server) {

	int i = 0;
	while (N == -1 || i < N) {
		testFastResPar1(server);
		i++;
	}
}

void testFastConnect(Server& server) {

	server.killAllAndWaitFor("fastpar");

	int i = 0;
	while (N == -1 || i < N) {

		for (int j = 0; j < 10; j++) {

			auto_ptr<application::Instance> result = server.start("fastpar");
			cout << "new application " << result->getNameId() << endl;
		}

		usleep(10000);

		application::InstanceArray rs = server.connectAll("fastpar");
		// testing copy/transfer
		application::InstanceArray results = rs;

		for (int j = 0; j < results.size(); j++) {
			results[j]->kill();
			application::State state = results[j]->waitFor();
			cout << "killed " << results[j]->getNameId()
					<< " with initial state " << application::toString(results[j]->getInitialState())
					<< " and final state " << application::toString(state) << endl;
		}

		i++;
	}
}

void testNoApp(Server& server) {

	int i = 0;
	while (N == -1 || i < N) {

		auto_ptr<application::Instance> result = server.start("noapp");
		cout << "new application " << result->getNameId() << endl;

		application::State state = result->waitFor();

		if (state == application::SUCCESS) {
			cout << "error, the application should fail" << endl;
		} else {
			cout << "ok, the application failed" << endl;
		}

		i++;
	}
}

void testNoApp(Server& server, int time) {

	int i = 0;
	while (N == -1 || i < N) {

		auto_ptr<application::Instance> result = server.start("noapp");
		cout << "new application " << result->getNameId() << endl;

		if (time > 0) {
			usleep(time * 1000);
		}

		application::State state = result->waitFor();

		if (state == application::SUCCESS) {
			cout << "error, the application should fail" << endl;
		} else {
			cout << "ok, the application failed" << endl;
		}

		i++;
	}
}

int main(int argc, char *argv[]) {

	if (argc < 3) {
		// ex: ./testcameo tcp://localhost:7000 fastout 1
		cout << "syntax: testcameo <endpoint> <test>\n";
		return -1;
	}

	Server server(argv[1]);
	cout << "testing connection of " << argv[1] << endl;

	// manage options
	string testArg = argv[2];

	if (argc >= 4) {
		istringstream is(argv[3]);
		is >> N;
	}

	// loop with fast application
	if (server.isAvailable()) {
		cout << "connection is ok" << endl;
	} else {
		cout << "no response from server" << endl;
		return -1;
	}

	if (testArg == "fast") {
		testFast(server);
	} else if (testArg == "fastasync") {
		testFastAsync(server);
	} else if (testArg == "fastres") {
		testFastRes(server);
	} else if (testArg == "fastrespar1") {
		testFastResPar1(server);
	} else if (testArg == "fastrespar") {
		testFastResPar(server);
	} else if (testArg == "fastconnect") {
		testFastConnect(server);
	} else if (testArg == "noapp") {
		testNoApp(server, 0);
	} else if (testArg == "noapp100") {
		testNoApp(server, 100);
	}

	return 0;
}
