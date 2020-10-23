#include "Test.h"
#include "../include/Strings.h"
#include "../include/JSON.h"
#include "../src/message/Message.h"
#include <iostream>

using namespace std;
using namespace cameo;

int main(int argc, char *argv[]) {

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
