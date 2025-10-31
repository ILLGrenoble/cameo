/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

#include <cameo/api/cameo.h>
#include <atomic>
#include <iostream>

using namespace std;
using namespace cameo;

int main(int argc, char *argv[]) {

	This::init(argc, argv);

	std::atomic_bool stopping(false);

	This::handleStop([&] {
		cout << "Stop handler executed" << endl;
		stopping.store(true);
	});

	This::setRunning();

	int i = 0;
	while (!stopping.load()) {
		cout << "Waiting " << i << "..." << endl;
		this_thread::sleep_for(chrono::milliseconds(100));
		i++;
	}

	This::setResult("2189");

	cout << "Finished the application" << endl;

	This::terminate();

	return 0;
}
