add_executable(teststrings TestStrings.cpp)

target_link_libraries(teststrings PUBLIC
	cameo
	)
	
add_test(cameo teststrings)