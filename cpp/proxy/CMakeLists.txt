#------------------------------------------------------------
# Project
#------------------------------------------------------------

cmake_minimum_required(VERSION 3.12.0)
  
# Project name and version
project(cameo-proxy VERSION 1.0.0 LANGUAGES CXX)

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

add_executable(cameo-rep-proxy src/RepProxy.cpp)
target_link_libraries(cameo-rep-proxy PRIVATE Cameo::cameo-api-cpp)

add_executable(cameo-pub-proxy src/PubProxy.cpp)
target_link_libraries(cameo-pub-proxy PRIVATE Cameo::cameo-api-cpp)