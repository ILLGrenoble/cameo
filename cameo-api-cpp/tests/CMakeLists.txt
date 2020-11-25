set(CMAKE_CXX_STANDARD 17)

add_executable(testendpoint TestEndpoint.cpp)

target_link_libraries(testendpoint PRIVATE
	cameo
	)
target_include_directories(testendpoint
  PRIVATE ${CMAKE_CURRENT_SOURCE_DIR}/../src/
  )
	
add_test(endpoint testendpoint)


add_executable(testapplicationidentity TestApplicationIdentity.cpp)
target_link_libraries(testapplicationidentity
  PRIVATE cameo
  )
target_include_directories(testapplicationidentity
  PRIVATE ${CMAKE_CURRENT_SOURCE_DIR}/../src/
  )
target_include_directories(testapplicationidentity
    SYSTEM PRIVATE ${Rapidjson_INCLUDE_DIRS}/
)

add_test(applicationidentity testapplicationidentity)


add_executable(testapplicationwithstarteridentity TestApplicationWithStarterIdentity.cpp)

target_link_libraries(testapplicationwithstarteridentity PRIVATE
	cameo
	)
target_include_directories(testapplicationwithstarteridentity
  PRIVATE ${CMAKE_CURRENT_SOURCE_DIR}/../src/
  )
target_include_directories(testapplicationwithstarteridentity
    SYSTEM PRIVATE ${Rapidjson_INCLUDE_DIRS}/
)
	
add_test(applicationwithstarteridentity testapplicationwithstarteridentity)
