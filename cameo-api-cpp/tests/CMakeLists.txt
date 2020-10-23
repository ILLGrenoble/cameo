set(CMAKE_CXX_STANDARD 17)

add_executable(testendpoint TestEndpoint.cpp)

target_link_libraries(testendpoint PUBLIC
	cameo
	)
	
add_test(endpoint testendpoint)


add_executable(testname TestName.cpp)

target_link_libraries(testname PUBLIC
	cameo
	)
	
add_test(name testname)


add_executable(testnameid TestNameId.cpp)

target_link_libraries(testnameid PUBLIC
	cameo
	)
	
add_test(nameid testnameid)


add_executable(testapplicationidentity TestApplicationIdentity.cpp)

target_link_libraries(testapplicationidentity PUBLIC
	cameo
	)
	
add_test(applicationidentity testapplicationidentity)


add_executable(testapplicationandstarteridentities TestApplicationAndStarterIdentities.cpp)

target_link_libraries(testapplicationandstarteridentities PUBLIC
	cameo
	)
	
add_test(applicationandstarteridentities testapplicationandstarteridentities)