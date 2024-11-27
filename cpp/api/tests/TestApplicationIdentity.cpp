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
#include "Messages.h"
#include "../src/base/JSON.h"

using namespace std;
using namespace cameo;

int main(int, char *[]) {

	Endpoint endpoint("gamma75", 9999);

	ApplicationIdentity identity("my-app", 31, endpoint);

	string jsonString = identity.toJSONString();

	json::Object jsonIdentity;
	json::parse(jsonIdentity, jsonString);

	CAMEO_ASSERT_TRUE(string("my-app") == jsonIdentity[message::ApplicationIdentity::NAME].GetString());
	CAMEO_ASSERT_EQUAL(31, jsonIdentity[message::ApplicationIdentity::ID].GetInt());
	CAMEO_ASSERT_TRUE(string("tcp://gamma75:9999") == jsonIdentity[message::ApplicationIdentity::SERVER].GetString());

	return 0;
}