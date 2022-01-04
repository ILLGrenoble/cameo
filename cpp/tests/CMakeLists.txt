#------------------------------------------------------------
# Project
#------------------------------------------------------------

cmake_minimum_required(VERSION 3.12.0)
  
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

add_executable(simple src/tests/Simple.cpp)
target_link_libraries(simple PRIVATE Cameo::cameo-api-cpp)

add_executable(testsimple src/tests/TestSimple.cpp)
target_link_libraries(testsimple PRIVATE Cameo::cameo-api-cpp)

add_executable(veryfast src/tests/VeryFast.cpp)
target_link_libraries(veryfast PRIVATE Cameo::cameo-api-cpp)

add_executable(testveryfast src/tests/TestVeryFast.cpp)
target_link_libraries(testveryfast PRIVATE Cameo::cameo-api-cpp)

add_executable(stop src/tests/Stop.cpp)
target_link_libraries(stop PRIVATE Cameo::cameo-api-cpp)

add_executable(teststop src/tests/TestStop.cpp)
target_link_libraries(teststop PRIVATE Cameo::cameo-api-cpp)

add_executable(result src/tests/Result.cpp)
target_link_libraries(result PRIVATE Cameo::cameo-api-cpp)

add_executable(testresult src/tests/TestResult.cpp)
target_link_libraries(testresult PRIVATE Cameo::cameo-api-cpp)	

add_executable(error src/tests/Error.cpp)
target_link_libraries(error PRIVATE Cameo::cameo-api-cpp)

add_executable(testerror src/tests/TestError.cpp)
target_link_libraries(testerror PRIVATE Cameo::cameo-api-cpp)

add_executable(teststorage src/tests/TestStorage.cpp)
target_link_libraries(teststorage PRIVATE Cameo::cameo-api-cpp)

add_executable(waitstorage src/tests/WaitStorage.cpp)
target_link_libraries(waitstorage PRIVATE Cameo::cameo-api-cpp)

add_executable(testwaitstorage src/tests/TestWaitStorage.cpp)
target_link_libraries(testwaitstorage PRIVATE Cameo::cameo-api-cpp)

add_executable(publisher src/tests/Publisher.cpp)
target_link_libraries(publisher PRIVATE Cameo::cameo-api-cpp)

add_executable(testpublisher src/tests/TestPublisher.cpp)
target_link_libraries(testpublisher PRIVATE Cameo::cameo-api-cpp)

add_executable(subscriber src/tests/Subscriber.cpp)
target_link_libraries(subscriber PRIVATE Cameo::cameo-api-cpp)

add_executable(testsubscriber src/tests/TestSubscriber.cpp)
target_link_libraries(testsubscriber PRIVATE Cameo::cameo-api-cpp)

add_executable(responder src/tests/Responder.cpp)
target_link_libraries(responder PRIVATE Cameo::cameo-api-cpp)

add_executable(testresponder src/tests/TestResponder.cpp)
target_link_libraries(testresponder PRIVATE Cameo::cameo-api-cpp)

add_executable(requester src/tests/Requester.cpp)
target_link_libraries(requester PRIVATE Cameo::cameo-api-cpp)

add_executable(testrequester src/tests/TestRequester.cpp)
target_link_libraries(testrequester PRIVATE Cameo::cameo-api-cpp)

add_executable(publisherloop src/tests/PublisherLoop.cpp)
target_link_libraries(publisherloop PRIVATE Cameo::cameo-api-cpp)

add_executable(testcancel src/tests/TestCancel.cpp)
target_link_libraries(testcancel PRIVATE Cameo::cameo-api-cpp  pthread)

add_executable(openport src/tests/OpenPort.cpp)
target_link_libraries(openport PRIVATE Cameo::cameo-api-cpp)

add_executable(comport src/tests/ComPort.cpp)
target_link_libraries(comport PRIVATE Cameo::cameo-api-cpp)

add_executable(testcomport src/tests/TestComPort.cpp)
target_link_libraries(testcomport PRIVATE Cameo::cameo-api-cpp)

add_executable(stream src/tests/Stream.cpp)
target_link_libraries(stream PRIVATE Cameo::cameo-api-cpp)

add_executable(teststream src/tests/TestStream.cpp)
target_link_libraries(teststream PRIVATE Cameo::cameo-api-cpp  pthread)


# Examples

add_executable(responderapp src/examples/ResponderApp.cpp)
target_link_libraries(responderapp PRIVATE Cameo::cameo-api-cpp)

add_executable(requesterapp src/examples/RequesterApp.cpp)
target_link_libraries(requesterapp PRIVATE Cameo::cameo-api-cpp)

add_executable(publisherapp src/examples/PublisherApp.cpp)
target_link_libraries(publisherapp PRIVATE Cameo::cameo-api-cpp)

add_executable(subscriberapp src/examples/SubscriberApp.cpp)
target_link_libraries(subscriberapp PRIVATE Cameo::cameo-api-cpp)
