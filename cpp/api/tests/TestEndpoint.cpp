/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

#include "Test.h"
#include "Strings.h"

using namespace std;
using namespace cameo;

int main(int, char *[]) {

	Endpoint endpoint("gamma75", 9999);

	CAMEO_ASSERT_TRUE("tcp://gamma75:9999" == endpoint.toString());

	endpoint = Endpoint::parse("tcp://gamma75:9999");

	CAMEO_ASSERT_TRUE("tcp" == endpoint.getProtocol());
	CAMEO_ASSERT_TRUE("gamma75" == endpoint.getAddress());
	CAMEO_ASSERT_EQUAL(9999, endpoint.getPort());

	endpoint = Endpoint::parse("ws://gamma75:9999");

	CAMEO_ASSERT_TRUE("ws" == endpoint.getProtocol());
	CAMEO_ASSERT_TRUE("gamma75" == endpoint.getAddress());
	CAMEO_ASSERT_EQUAL(9999, endpoint.getPort());

	endpoint = Endpoint::parse("tcp://175.29.285.15:9999");

	CAMEO_ASSERT_TRUE("tcp" == endpoint.getProtocol());
	CAMEO_ASSERT_TRUE("175.29.285.15" == endpoint.getAddress());
	CAMEO_ASSERT_EQUAL(9999, endpoint.getPort());

	Endpoint endpoint2("tcp", "175.29.285.15", 9999);

	CAMEO_ASSERT_TRUE(endpoint == endpoint2);

	bool error = false;
	try {
		Endpoint::parse("gamma75:9999");
	}
	catch (...) {
		error = true;
	}
	CAMEO_ASSERT_TRUE(error);

	error = false;
	try {
		Endpoint::parse("tcp://gamma75:");
	}
	catch (...) {
		error = true;
	}
	CAMEO_ASSERT_TRUE(error);

	error = false;
	try {
		Endpoint::parse("tcp:/gamma75:9999");
	}
	catch (...) {
		error = true;
	}
	CAMEO_ASSERT_TRUE(error);

	error = false;
	try {
		Endpoint::parse("tcp://:9999");
	}
	catch (...) {
		error = true;
	}
	CAMEO_ASSERT_TRUE(error);

	return 0;
}