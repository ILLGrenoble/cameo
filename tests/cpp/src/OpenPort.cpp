/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
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

	zmq::socket_t socket(context, zmq::socket_type::rep);

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