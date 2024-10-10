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

add_executable(testservererror src/TestServerError.cpp)
target_link_libraries(testservererror PRIVATE Cameo::cameo-api-cpp-static)

add_executable(testserver src/TestServer.cpp)
target_link_libraries(testserver PRIVATE Cameo::cameo-api-cpp-static)

add_executable(testbadendpoint src/TestBadEndpoint.cpp)
target_link_libraries(testbadendpoint PRIVATE Cameo::cameo-api-cpp-static)

add_executable(testappexceptions src/TestAppExceptions.cpp)
target_link_libraries(testappexceptions PRIVATE Cameo::cameo-api-cpp-static)

add_executable(simple src/Simple.cpp)
target_link_libraries(simple PRIVATE Cameo::cameo-api-cpp-static)

add_executable(testsimple src/TestSimple.cpp)
target_link_libraries(testsimple PRIVATE Cameo::cameo-api-cpp-static)

add_executable(veryfast src/VeryFast.cpp)
target_link_libraries(veryfast PRIVATE Cameo::cameo-api-cpp-static)

add_executable(testveryfast src/TestVeryFast.cpp)
target_link_libraries(testveryfast PRIVATE Cameo::cameo-api-cpp-static)

add_executable(stop src/Stop.cpp)
target_link_libraries(stop PRIVATE Cameo::cameo-api-cpp-static)

add_executable(teststop src/TestStop.cpp)
target_link_libraries(teststop PRIVATE Cameo::cameo-api-cpp-static)

add_executable(result src/Result.cpp)
target_link_libraries(result PRIVATE Cameo::cameo-api-cpp-static)

add_executable(testresult src/TestResult.cpp)
target_link_libraries(testresult PRIVATE Cameo::cameo-api-cpp-static)	

add_executable(error src/Error.cpp)
target_link_libraries(error PRIVATE Cameo::cameo-api-cpp-static)

add_executable(testerror src/TestError.cpp)
target_link_libraries(testerror PRIVATE Cameo::cameo-api-cpp-static)

add_executable(teststorage src/TestStorage.cpp)
target_link_libraries(teststorage PRIVATE Cameo::cameo-api-cpp-static)

add_executable(waitstorage src/WaitStorage.cpp)
target_link_libraries(waitstorage PRIVATE Cameo::cameo-api-cpp-static)

add_executable(testwaitstorage src/TestWaitStorage.cpp)
target_link_libraries(testwaitstorage PRIVATE Cameo::cameo-api-cpp-static)

add_executable(publisher src/Publisher.cpp)
target_link_libraries(publisher PRIVATE Cameo::cameo-api-cpp-static)

add_executable(testpublisher src/TestPublisher.cpp)
target_link_libraries(testpublisher PRIVATE Cameo::cameo-api-cpp-static)

add_executable(subscriber src/Subscriber.cpp)
target_link_libraries(subscriber PRIVATE Cameo::cameo-api-cpp-static)

add_executable(testsubscriber src/TestSubscriber.cpp)
target_link_libraries(testsubscriber PRIVATE Cameo::cameo-api-cpp-static)

add_executable(publisherloop src/PublisherLoop.cpp)
target_link_libraries(publisherloop PRIVATE Cameo::cameo-api-cpp-static)

add_executable(testcancel src/TestCancel.cpp)
target_link_libraries(testcancel PRIVATE Cameo::cameo-api-cpp-static)

#add_executable(openport src/OpenPort.cpp)
#target_link_libraries(openport PRIVATE Cameo::cameo-api-cpp-static)

add_executable(comport src/ComPort.cpp)
target_link_libraries(comport PRIVATE Cameo::cameo-api-cpp-static)

add_executable(testcomport src/TestComPort.cpp)
target_link_libraries(testcomport PRIVATE Cameo::cameo-api-cpp-static)

add_executable(stream src/Stream.cpp)
target_link_libraries(stream PRIVATE Cameo::cameo-api-cpp-static)

add_executable(teststream src/TestStream.cpp)
target_link_libraries(teststream PRIVATE Cameo::cameo-api-cpp-static)

add_executable(basicresponder src/BasicResponder.cpp)
target_link_libraries(basicresponder PRIVATE Cameo::cameo-api-cpp-static)

add_executable(testbasicresponder src/TestBasicResponder.cpp)
target_link_libraries(testbasicresponder PRIVATE Cameo::cameo-api-cpp-static)

add_executable(basicrequester src/BasicRequester.cpp)
target_link_libraries(basicrequester PRIVATE Cameo::cameo-api-cpp-static)

add_executable(testbasicrequester src/TestBasicRequester.cpp)
target_link_libraries(testbasicrequester PRIVATE Cameo::cameo-api-cpp-static)

add_executable(testcheckapp src/TestCheckApp.cpp)
target_link_libraries(testcheckapp PRIVATE Cameo::cameo-api-cpp-static)

add_executable(multiresponder src/MultiResponder.cpp)
target_link_libraries(multiresponder PRIVATE Cameo::cameo-api-cpp-static)

add_executable(testmultiresponder src/TestMultiResponder.cpp)
target_link_libraries(testmultiresponder PRIVATE Cameo::cameo-api-cpp-static)

add_executable(multiresponders src/MultiResponders.cpp)
target_link_libraries(multiresponders PRIVATE Cameo::cameo-api-cpp-static)

add_executable(testmultiresponders src/TestMultiResponders.cpp)
target_link_libraries(testmultiresponders PRIVATE Cameo::cameo-api-cpp-static)

add_executable(linked src/Linked.cpp)
target_link_libraries(linked PRIVATE Cameo::cameo-api-cpp-static)

add_executable(testlinked src/TestLinked.cpp)
target_link_libraries(testlinked PRIVATE Cameo::cameo-api-cpp-static)

add_executable(comstimeout src/ComsTimeout.cpp)
target_link_libraries(comstimeout PRIVATE Cameo::cameo-api-cpp-static)

add_executable(testcomstimeout src/TestComsTimeout.cpp)
target_link_libraries(testcomstimeout PRIVATE Cameo::cameo-api-cpp-static)

add_executable(respondererror src/ResponderError.cpp)
target_link_libraries(respondererror PRIVATE Cameo::cameo-api-cpp-static)

add_executable(testrespondererror src/TestResponderError.cpp)
target_link_libraries(testrespondererror PRIVATE Cameo::cameo-api-cpp-static)

add_executable(publishererror src/PublisherError.cpp)
target_link_libraries(publishererror PRIVATE Cameo::cameo-api-cpp-static)

add_executable(testpublishererror src/TestPublisherError.cpp)
target_link_libraries(testpublishererror PRIVATE Cameo::cameo-api-cpp-static)

add_executable(testsubscribertimeout src/TestSubscriberTimeout.cpp)
target_link_libraries(testsubscribertimeout PRIVATE Cameo::cameo-api-cpp-static)

add_executable(remoteserver src/RemoteServer.cpp)
target_link_libraries(remoteserver PRIVATE Cameo::cameo-api-cpp-static)

add_executable(testremoteserver src/TestRemoteServer.cpp)
target_link_libraries(testremoteserver PRIVATE Cameo::cameo-api-cpp-static)
