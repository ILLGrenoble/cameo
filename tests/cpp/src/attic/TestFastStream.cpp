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
#include <chrono>
#include <thread>

using namespace std;

int main(int argc, char *argv[]) {

	int t = 1E6;
	int n = 5;

	if (argc > 1) {
		t = stoi(argv[1]);
	}

	if (argc > 2) {
		n = stoi(argv[2]);
	}

	for (int i = 0; i < n; i++) {

		cout << "line " << i << " ";
		int c = i % 10;
		for (int j = 0; j < c; ++j) {
			cout << "*";
		}
		cout << endl;
		if (c == 0) {
			cout << endl;
		}

		this_thread::sleep_for(chrono::microseconds(t));
	}
	cout << "OOOO" << endl;

	return 0;
}
