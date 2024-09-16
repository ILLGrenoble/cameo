#------------------------------------------------------------
# Project
#------------------------------------------------------------

cmake_minimum_required(VERSION 3.20.0)
  
# Project name and version
project(cameo-tests VERSION 1.0.0 LANGUAGES CXX)

include(GNUInstallDirs)
include(CMakePackageConfigHelpers)
include(FetchContent)

if (NOT DEFINED CMAKE_CXX_STANDARD)
  # specify the C++ standard
  set(CMAKE_CXX_STANDARD 17)
  set(CMAKE_CXX_STANDARD_REQUIRED True)
endif()

#------------------------------------------------------------
# Programs
#------------------------------------------------------------

# Tests

add_executable(testservererror src/TestServerError.cpp ${Rapidjson_INCLUDE_DIRS})
target_link_libraries(testservererror PRIVATE Cameo::cameo-api-cpp-static)

add_executable(testserver src/TestServer.cpp ${Rapidjson_INCLUDE_DIRS})
target_link_libraries(testserver PRIVATE Cameo::cameo-api-cpp-static)

add_executable(testbadendpoint src/TestBadEndpoint.cpp ${Rapidjson_INCLUDE_DIRS})
target_link_libraries(testbadendpoint PRIVATE Cameo::cameo-api-cpp-static)

add_executable(testappexceptions src/TestAppExceptions.cpp ${Rapidjson_INCLUDE_DIRS})
target_link_libraries(testappexceptions PRIVATE Cameo::cameo-api-cpp-static)

add_executable(simple src/Simple.cpp ${Rapidjson_INCLUDE_DIRS})
target_link_libraries(simple PRIVATE Cameo::cameo-api-cpp-static)

add_executable(testsimple src/TestSimple.cpp ${Rapidjson_INCLUDE_DIRS})
target_link_libraries(testsimple PRIVATE Cameo::cameo-api-cpp-static)

add_executable(veryfast src/VeryFast.cpp ${Rapidjson_INCLUDE_DIRS})
target_link_libraries(veryfast PRIVATE Cameo::cameo-api-cpp-static)

add_executable(testveryfast src/TestVeryFast.cpp ${Rapidjson_INCLUDE_DIRS})
target_link_libraries(testveryfast PRIVATE Cameo::cameo-api-cpp-static)

add_executable(stop src/Stop.cpp ${Rapidjson_INCLUDE_DIRS})
target_link_libraries(stop PRIVATE Cameo::cameo-api-cpp-static)

add_executable(teststop src/TestStop.cpp ${Rapidjson_INCLUDE_DIRS})
target_link_libraries(teststop PRIVATE Cameo::cameo-api-cpp-static)

add_executable(result src/Result.cpp ${Rapidjson_INCLUDE_DIRS})
target_link_libraries(result PRIVATE Cameo::cameo-api-cpp-static)

add_executable(testresult src/TestResult.cpp ${Rapidjson_INCLUDE_DIRS})
target_link_libraries(testresult PRIVATE Cameo::cameo-api-cpp-static)	

add_executable(error src/Error.cpp ${Rapidjson_INCLUDE_DIRS})
target_link_libraries(error PRIVATE Cameo::cameo-api-cpp-static)

add_executable(testerror src/TestError.cpp ${Rapidjson_INCLUDE_DIRS})
target_link_libraries(testerror PRIVATE Cameo::cameo-api-cpp-static)

add_executable(teststorage src/TestStorage.cpp ${Rapidjson_INCLUDE_DIRS})
target_link_libraries(teststorage PRIVATE Cameo::cameo-api-cpp-static)

add_executable(waitstorage src/WaitStorage.cpp ${Rapidjson_INCLUDE_DIRS})
target_link_libraries(waitstorage PRIVATE Cameo::cameo-api-cpp-static)

add_executable(testwaitstorage src/TestWaitStorage.cpp ${Rapidjson_INCLUDE_DIRS})
target_link_libraries(testwaitstorage PRIVATE Cameo::cameo-api-cpp-static)

add_executable(publisher src/Publisher.cpp ${Rapidjson_INCLUDE_DIRS})
target_link_libraries(publisher PRIVATE Cameo::cameo-api-cpp-static)

add_executable(testpublisher src/TestPublisher.cpp ${Rapidjson_INCLUDE_DIRS})
target_link_libraries(testpublisher PRIVATE Cameo::cameo-api-cpp-static)

add_executable(subscriber src/Subscriber.cpp ${Rapidjson_INCLUDE_DIRS})
target_link_libraries(subscriber PRIVATE Cameo::cameo-api-cpp-static)

add_executable(testsubscriber src/TestSubscriber.cpp ${Rapidjson_INCLUDE_DIRS})
target_link_libraries(testsubscriber PRIVATE Cameo::cameo-api-cpp-static)

add_executable(publisherloop src/PublisherLoop.cpp ${Rapidjson_INCLUDE_DIRS})
target_link_libraries(publisherloop PRIVATE Cameo::cameo-api-cpp-static)

add_executable(testcancel src/TestCancel.cpp ${Rapidjson_INCLUDE_DIRS})
target_link_libraries(testcancel PRIVATE Cameo::cameo-api-cpp  pthread)

add_executable(openport src/OpenPort.cpp ${Rapidjson_INCLUDE_DIRS})
target_link_libraries(openport PRIVATE Cameo::cameo-api-cpp-static)

add_executable(comport src/ComPort.cpp ${Rapidjson_INCLUDE_DIRS})
target_link_libraries(comport PRIVATE Cameo::cameo-api-cpp-static)

add_executable(testcomport src/TestComPort.cpp ${Rapidjson_INCLUDE_DIRS})
target_link_libraries(testcomport PRIVATE Cameo::cameo-api-cpp-static)

add_executable(stream src/Stream.cpp ${Rapidjson_INCLUDE_DIRS})
target_link_libraries(stream PRIVATE Cameo::cameo-api-cpp-static)

add_executable(teststream src/TestStream.cpp ${Rapidjson_INCLUDE_DIRS})
target_link_libraries(teststream PRIVATE Cameo::cameo-api-cpp  pthread)

add_executable(basicresponder src/BasicResponder.cpp ${Rapidjson_INCLUDE_DIRS})
target_link_libraries(basicresponder PRIVATE Cameo::cameo-api-cpp-static)

add_executable(testbasicresponder src/TestBasicResponder.cpp ${Rapidjson_INCLUDE_DIRS})
target_link_libraries(testbasicresponder PRIVATE Cameo::cameo-api-cpp-static)

add_executable(basicrequester src/BasicRequester.cpp ${Rapidjson_INCLUDE_DIRS})
target_link_libraries(basicrequester PRIVATE Cameo::cameo-api-cpp-static)

add_executable(testbasicrequester src/TestBasicRequester.cpp ${Rapidjson_INCLUDE_DIRS})
target_link_libraries(testbasicrequester PRIVATE Cameo::cameo-api-cpp-static)

add_executable(testcheckapp src/TestCheckApp.cpp ${Rapidjson_INCLUDE_DIRS})
target_link_libraries(testcheckapp PRIVATE Cameo::cameo-api-cpp-static)

add_executable(multiresponder src/MultiResponder.cpp ${Rapidjson_INCLUDE_DIRS})
target_link_libraries(multiresponder PRIVATE Cameo::cameo-api-cpp-static)

add_executable(testmultiresponder src/TestMultiResponder.cpp ${Rapidjson_INCLUDE_DIRS})
target_link_libraries(testmultiresponder PRIVATE Cameo::cameo-api-cpp-static)

add_executable(multiresponders src/MultiResponders.cpp ${Rapidjson_INCLUDE_DIRS})
target_link_libraries(multiresponders PRIVATE Cameo::cameo-api-cpp-static)

add_executable(testmultiresponders src/TestMultiResponders.cpp ${Rapidjson_INCLUDE_DIRS})
target_link_libraries(testmultiresponders PRIVATE Cameo::cameo-api-cpp-static)

add_executable(linked src/Linked.cpp ${Rapidjson_INCLUDE_DIRS})
target_link_libraries(linked PRIVATE Cameo::cameo-api-cpp-static)

add_executable(testlinked src/TestLinked.cpp ${Rapidjson_INCLUDE_DIRS})
target_link_libraries(testlinked PRIVATE Cameo::cameo-api-cpp-static)

add_executable(comstimeout src/ComsTimeout.cpp ${Rapidjson_INCLUDE_DIRS})
target_link_libraries(comstimeout PRIVATE Cameo::cameo-api-cpp-static)

add_executable(testcomstimeout src/TestComsTimeout.cpp ${Rapidjson_INCLUDE_DIRS})
target_link_libraries(testcomstimeout PRIVATE Cameo::cameo-api-cpp-static)

add_executable(respondererror src/ResponderError.cpp ${Rapidjson_INCLUDE_DIRS})
target_link_libraries(respondererror PRIVATE Cameo::cameo-api-cpp-static)

add_executable(testrespondererror src/TestResponderError.cpp ${Rapidjson_INCLUDE_DIRS})
target_link_libraries(testrespondererror PRIVATE Cameo::cameo-api-cpp-static)

add_executable(publishererror src/PublisherError.cpp ${Rapidjson_INCLUDE_DIRS})
target_link_libraries(publishererror PRIVATE Cameo::cameo-api-cpp-static)

add_executable(testpublishererror src/TestPublisherError.cpp ${Rapidjson_INCLUDE_DIRS})
target_link_libraries(testpublishererror PRIVATE Cameo::cameo-api-cpp-static)

add_executable(testsubscribertimeout src/TestSubscriberTimeout.cpp ${Rapidjson_INCLUDE_DIRS})
target_link_libraries(testsubscribertimeout PRIVATE Cameo::cameo-api-cpp-static)
