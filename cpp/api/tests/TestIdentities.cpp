#include "Test.h"
#include "Strings.h"
#include <iostream>

using namespace std;
using namespace cameo;

int main(int, char *[]) {

//	CAMEO_ASSERT_TRUE("tcp://gamma75:9999" == endpoint.toString());
//	CAMEO_ASSERT_EQUAL(9999, endpoint.getPort());

	ServerIdentity server {"tcp://gamma75:9999", true};

	cout << "server = " << server.toJSONString() << endl;

	AppIdentity app {"my-app", 12, server};

	cout << "app = " << app.toJSONString() << endl;

	return 0;
}