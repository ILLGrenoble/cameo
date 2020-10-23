#include "Test.h"
#include "../include/Strings.h"
#include <iostream>

using namespace std;
using namespace cameo;

int main(int argc, char *argv[]) {

	CAMEO_ASSERT_TRUE("my-app.31@tcp://gamma75:9999" == ApplicationIdentity(NameId("my-app", 31), Endpoint("gamma75", 9999)).toString());
	CAMEO_ASSERT_TRUE("my-app@tcp://gamma75:9999" == ApplicationIdentity(NameId("my-app"), Endpoint("gamma75", 9999)).toString());

	ApplicationIdentity identity = ApplicationIdentity::parse("my-app.31@tcp://gamma75:9999");

	CAMEO_ASSERT_TRUE("my-app" == identity.getNameId().getName());
	CAMEO_ASSERT_TRUE(identity.getNameId().getId().has_value());
	CAMEO_ASSERT_EQUAL(31, identity.getNameId().getId().value());
	CAMEO_ASSERT_TRUE("gamma75" == identity.getEndpoint().getAddress());
	CAMEO_ASSERT_EQUAL(9999, identity.getEndpoint().getPort());

	identity = ApplicationIdentity::parse("my-app.31@tcp://127.65.198.1:9999");

	CAMEO_ASSERT_TRUE("my-app" == identity.getNameId().getName());
	CAMEO_ASSERT_TRUE(identity.getNameId().getId().has_value());
	CAMEO_ASSERT_EQUAL(31, identity.getNameId().getId().value());
	CAMEO_ASSERT_TRUE("127.65.198.1" == identity.getEndpoint().getAddress());
	CAMEO_ASSERT_EQUAL(9999, identity.getEndpoint().getPort());

	identity = ApplicationIdentity::parse("my-app@tcp://gamma75:9999");

	CAMEO_ASSERT_TRUE("my-app" == identity.getNameId().getName());
	CAMEO_ASSERT_TRUE(!identity.getNameId().getId().has_value());
	CAMEO_ASSERT_TRUE("gamma75" == identity.getEndpoint().getAddress());
	CAMEO_ASSERT_EQUAL(9999, identity.getEndpoint().getPort());

	bool error = false;
	try {
		ApplicationIdentity::parse("my-app.ff@tcp://gamma75:9999");
	}
	catch (...) {
		error = true;
	}
	CAMEO_ASSERT_TRUE(error);

	error = false;
	try {
		ApplicationIdentity::parse("my-app.ff@tcp:/gamma75:9999");
	}
	catch (...) {
		error = true;
	}
	CAMEO_ASSERT_TRUE(error);

	error = false;
	try {
		ApplicationIdentity::parse("my-app.ff@tcp://gamma75:99G");
	}
	catch (...) {
		error = true;
	}
	CAMEO_ASSERT_TRUE(error);

	return 0;
}
