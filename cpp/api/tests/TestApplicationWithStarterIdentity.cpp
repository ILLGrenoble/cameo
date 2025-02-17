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

	Endpoint myEndpoint("gamma75", 9000);
	ApplicationIdentity application("my-app", 31, myEndpoint);

	Endpoint yourEndpoint("gamma57", 7000);
	ApplicationIdentity starter("your-app", 76, yourEndpoint);

	ApplicationWithStarterIdentity identity(application, starter);

	string jsonString = identity.toJSONString();

	json::Object jsonIdentity;
	json::parse(jsonIdentity, jsonString);

	CAMEO_ASSERT_TRUE(string("my-app") == jsonIdentity[message::ApplicationIdentity::NAME].GetString());
	CAMEO_ASSERT_EQUAL(31, jsonIdentity[message::ApplicationIdentity::ID].GetInt());
	CAMEO_ASSERT_TRUE(string("tcp://gamma75:9000") == jsonIdentity[message::ApplicationIdentity::SERVER].GetString());

	CAMEO_ASSERT_TRUE(jsonIdentity.HasMember(message::ApplicationIdentity::STARTER));

	CAMEO_ASSERT_TRUE(string("your-app") == jsonIdentity[message::ApplicationIdentity::STARTER][message::ApplicationIdentity::NAME].GetString());
	CAMEO_ASSERT_EQUAL(76, jsonIdentity[message::ApplicationIdentity::STARTER][message::ApplicationIdentity::ID].GetInt());
	CAMEO_ASSERT_TRUE(string("tcp://gamma57:7000") == jsonIdentity[message::ApplicationIdentity::STARTER][message::ApplicationIdentity::SERVER].GetString());

	ApplicationWithStarterIdentity identity2(application);

	jsonString = identity2.toJSONString();
	json::parse(jsonIdentity, jsonString);

	CAMEO_ASSERT_TRUE(!jsonIdentity.HasMember(message::ApplicationIdentity::STARTER));

	return 0;
}