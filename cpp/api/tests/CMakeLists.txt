set(CMAKE_CXX_STANDARD 17)

add_executable(testendpoint TestEndpoint.cpp)
target_link_libraries(testendpoint PRIVATE Cameo::cameo-api-cpp-static)
target_include_directories(testendpoint PRIVATE ${CMAKE_CURRENT_SOURCE_DIR}/../include/ )
add_test(endpoint testendpoint)

add_executable(testapplicationidentity TestApplicationIdentity.cpp)
target_link_libraries(testapplicationidentity PRIVATE Cameo::cameo-api-cpp-static)
target_include_directories(testapplicationidentity PRIVATE ${CMAKE_CURRENT_SOURCE_DIR}/../include/ )
target_include_directories(testapplicationidentity SYSTEM PRIVATE ${RapidJSON_INCLUDE_DIRS}/ )
add_test(applicationidentity testapplicationidentity)

add_executable(testapplicationwithstarteridentity TestApplicationWithStarterIdentity.cpp)
target_link_libraries(testapplicationwithstarteridentity PRIVATE Cameo::cameo-api-cpp-static)
target_include_directories(testapplicationwithstarteridentity PRIVATE ${CMAKE_CURRENT_SOURCE_DIR}/../include/ )
target_include_directories(testapplicationwithstarteridentity SYSTEM PRIVATE ${RapidJSON_INCLUDE_DIRS}/ )
add_test(applicationwithstarteridentity testapplicationwithstarteridentity)

add_executable(testidentities TestIdentities.cpp)
target_link_libraries(testidentities PRIVATE Cameo::cameo-api-cpp-static)
target_include_directories(testidentities PRIVATE ${CMAKE_CURRENT_SOURCE_DIR}/../include/ )
target_include_directories(testidentities SYSTEM PRIVATE ${RapidJSON_INCLUDE_DIRS}/ )
add_test(identities testidentities)
