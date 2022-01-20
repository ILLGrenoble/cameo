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

#include <zmq.hpp>
#include <iostream>

using namespace std;

int main(int argc, char *argv[]) {

	if (argc < 2) {
		cout << "Usage: <port>" << endl;
		return 1;
	}

	string port(argv[1]);

	zmq::context_t context(1);

	zmq::socket_t socket(context, ZMQ_REP);

	string endpoint("tcp://*:");
	endpoint += port;

	cout << "Trying to bind " << endpoint << endl;

	try {
		socket.bind(endpoint.c_str());
	}
	catch (const exception& e) {
		cout << "Cannot bind port: " << e.what() << endl;
		return 1;
	}

	cout << "Bound port " << port << endl;

	int c;
	cin >> c;

	return 0;
}
