set(CMAKE_CXX_STANDARD 17)

add_executable(testendpoint TestEndpoint.cpp)

target_link_libraries(testendpoint PUBLIC
	cameo
	)
	
add_test(endpoint testendpoint)


add_executable(testapplicationidentity TestApplicationIdentity.cpp)

target_link_libraries(testapplicationidentity PUBLIC
	cameo
	)
	
add_test(applicationidentity testapplicationidentity)


add_executable(testapplicationwithstarteridentity TestApplicationWithStarterIdentity.cpp)

target_link_libraries(testapplicationwithstarteridentity PUBLIC
	cameo
	)
	
add_test(applicationwithstarteridentity testapplicationwithstarteridentity)
