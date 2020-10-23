#include "Test.h"
#include "../include/Strings.h"
#include <iostream>

using namespace std;
using namespace cameo;

int main(int argc, char *argv[]) {

	CAMEO_ASSERT_TRUE(Name::check("myapp"));
	CAMEO_ASSERT_TRUE(Name::check("MyApp"));
	CAMEO_ASSERT_TRUE(Name::check("MyApp0"));
	CAMEO_ASSERT_TRUE(Name::check("My-App0"));
	CAMEO_ASSERT_TRUE(Name::check("My_App0"));

	CAMEO_ASSERT_TRUE(!Name::check("myapp!"));
	CAMEO_ASSERT_TRUE(!Name::check("my app"));

	return 0;
}
