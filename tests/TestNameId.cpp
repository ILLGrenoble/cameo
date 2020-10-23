#include "Test.h"
#include "../include/Strings.h"
#include <iostream>

using namespace std;
using namespace cameo;

int main(int argc, char *argv[]) {

	CAMEO_ASSERT_TRUE("my-app.31" == NameId("my-app", 31).toString());

	NameId nameId = NameId::parse("my-app.31");

	CAMEO_ASSERT_TRUE("my-app" == nameId.getName());
	CAMEO_ASSERT_TRUE(nameId.getId().has_value());
	CAMEO_ASSERT_EQUAL(31, nameId.getId().value());

	nameId = NameId::parse("my-app32");
	CAMEO_ASSERT_TRUE(!nameId.getId().has_value());

	bool error = false;
	try {
		NameId::parse("my-app.ff");
	}
	catch (...) {
		error = true;
	}
	CAMEO_ASSERT_TRUE(error);

	return 0;
}
