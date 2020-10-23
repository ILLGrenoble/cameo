#include "Test.h"
#include "../include/Strings.h"
#include <iostream>

using namespace std;
using namespace cameo;

int main(int argc, char *argv[]) {

	Endpoint endpoint("gamma75", 9999);
	cout << endpoint.toString() << endl;

	ALEPH_ASSERT_EQUAL(1, 1);

	return 0;
}
