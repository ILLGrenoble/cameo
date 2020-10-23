#include "Test.h"
#include "../include/Strings.h"
#include <iostream>

using namespace std;
using namespace cameo;

int main(int argc, char *argv[]) {

	ApplicationAndStarterIdentities identities = ApplicationAndStarterIdentities::parse("my-app.31@tcp://gamma75:9999:your-app.15@tcp://gamma63:789");

	CAMEO_ASSERT_TRUE("my-app.31@tcp://gamma75:9999" == identities.getApplication().toString());
	CAMEO_ASSERT_TRUE(identities.getStarter().has_value());
	CAMEO_ASSERT_TRUE("your-app.15@tcp://gamma63:789" == identities.getStarter().value().toString());

	identities = ApplicationAndStarterIdentities::parse("my-app.31@tcp://gamma75:9999:");

	CAMEO_ASSERT_TRUE("my-app.31@tcp://gamma75:9999" == identities.getApplication().toString());
	CAMEO_ASSERT_TRUE(!identities.getStarter().has_value());

	bool error = false;
	try {
		ApplicationAndStarterIdentities::parse("my-app.31@tcp://gamma75:9999");
	}
	catch (...) {
		error = true;
	}
	CAMEO_ASSERT_TRUE(error);

	return 0;
}
