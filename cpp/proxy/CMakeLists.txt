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
# Program
#------------------------------------------------------------

add_executable(cameoproxy src/Proxy.cpp)
target_link_libraries(cameoproxy PRIVATE Cameo::cameo-api-cpp)