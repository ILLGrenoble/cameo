set(CMAKE_CXX_STANDARD 17)

add_executable(testendpoint TestEndpoint.cpp)

target_link_libraries(testendpoint PUBLIC
	cameo
	)
	
add_test(endpoint testendpoint)
