#include "Test.h"
#include "../include/Strings.h"
#include <iostream>

using namespace std;
using namespace cameo;

int main(int argc, char *argv[]) {

	Endpoint endpoint("gamma75", 9999);

	CAMEO_ASSERT_TRUE("gamma75:9999" == endpoint.toString());

	endpoint = Endpoint::parse("gamma75:9999");

	CAMEO_ASSERT_TRUE("gamma75" == endpoint.getAddress());
	CAMEO_ASSERT_EQUAL(9999, endpoint.getPort());

	endpoint = Endpoint::parse("175.29.285.15:9999");

	CAMEO_ASSERT_TRUE("175.29.285.15" == endpoint.getAddress());
	CAMEO_ASSERT_EQUAL(9999, endpoint.getPort());

	bool error = false;
	try {
		Endpoint::parse("gamma:75:9999");
	}
	catch (...) {
		error = true;
	}

	CAMEO_ASSERT_TRUE(error);

	error = false;
	try {
		Endpoint::parse("gamma75:");
	}
	catch (...) {
		error = true;
	}

	CAMEO_ASSERT_TRUE(error);

	return 0;
}
