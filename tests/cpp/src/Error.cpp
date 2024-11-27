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
#include <iostream>
#include <chrono>

using namespace std;
using namespace cameo;

int main(int, char *[]) {

	this_thread::sleep_for(chrono::milliseconds(10));

	cout << "Error" << endl;

	return 123;
}