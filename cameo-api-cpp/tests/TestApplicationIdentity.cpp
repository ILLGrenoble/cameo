#include "Test.h"
#include "Strings.h"
#include "message/Message.h"
#include <iostream>
#include "../include/JSON.h"

using namespace std;
using namespace cameo;

int main(int argc, char *argv[]) {

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
