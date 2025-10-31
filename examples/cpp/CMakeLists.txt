cmake_minimum_required(VERSION 3.20.0)
  
# Project name and version
project(cameo-examples VERSION 1.0.0 LANGUAGES CXX)

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

add_executable(responderapp src/ResponderApp.cpp)
target_link_libraries(responderapp PRIVATE Cameo::cameo-api-cpp)
if(NOT BUILD_SHARED_LIBS)
  target_compile_definitions(responderapp PUBLIC CAMEO_STATIC)
endif()

add_executable(requesterapp src/RequesterApp.cpp)
target_link_libraries(requesterapp PRIVATE Cameo::cameo-api-cpp)
if(NOT BUILD_SHARED_LIBS)
  target_compile_definitions(requesterapp PUBLIC CAMEO_STATIC)
endif()

add_executable(publisherapp src/PublisherApp.cpp)
target_link_libraries(publisherapp PRIVATE Cameo::cameo-api-cpp)
if(NOT BUILD_SHARED_LIBS)
  target_compile_definitions(publisherapp PUBLIC CAMEO_STATIC)
endif()

add_executable(subscriberapp src/SubscriberApp.cpp)
target_link_libraries(subscriberapp PRIVATE Cameo::cameo-api-cpp)
if(NOT BUILD_SHARED_LIBS)
  target_compile_definitions(subscriberapp PUBLIC CAMEO_STATIC)
endif()