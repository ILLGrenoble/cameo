cmake_minimum_required(VERSION 3.20.0)

project(cameo
  VERSION 2.2.0
  LANGUAGES C 
  HOMEPAGE_URL "https://code.ill.fr/cameo/cameo"
  )

option(CAMEO_API_CPP "Compile and install cameo C++ api" OFF)
option(CAMEO_API_PYTHON "Compile and install cameo Python api" OFF)
option(CAMEO_TESTS "Compile the tests" OFF)
option(CAMEO_EXAMPLES "Compile the examples" OFF)
set(ZEROMQ_JAVA "jzmq" CACHE STRING "ZeroMQ Java implementation (jzmq/jeromq)")

if(NOT CMAKE_BUILD_TYPE)
  set(CMAKE_BUILD_TYPE "Release") # set Release by default
endif()
message(STATUS "Build type: ${CMAKE_BUILD_TYPE}")

list(APPEND Proxies cpp/proxy)

if(${CAMEO_API_CPP})
  list(APPEND APIs cpp/api)
endif()

if(${CAMEO_TESTS})

  if(${ZEROMQ_JAVA} STREQUAL "jzmq")
    list(APPEND TestsExamples tests/java/jzmq)
  elseif(${ZEROMQ_JAVA} STREQUAL "jeromq")
    list(APPEND TestsExamples tests/java/jeromq)
  endif()    
  
  list(APPEND TestsExamples tests/cpp)
endif()

if(${CAMEO_EXAMPLES})

  if(${ZEROMQ_JAVA} STREQUAL "jzmq")
    list(APPEND TestsExamples examples/java/jzmq)
  elseif(${ZEROMQ_JAVA} STREQUAL "jeromq")
    list(APPEND TestsExamples examples/java/jeromq)
  endif()
  
  list(APPEND TestsExamples examples/cpp)
endif()

if(${CAMEO_API_PYTHON})
  list(APPEND APIs cpp/api)
  mark_as_advanced(CAMEO_API_CPP)
  list(APPEND APIs python/api)
endif()
list(REMOVE_DUPLICATES APIs)
#------------------------------------------------------------
# This is a trick: for the subprojects metioned here,
# the find_package does nothing, since the targets will be defined
# by the add_subdirectory command
#set(as_subproject cameo-api-cpp-static)
#macro(find_package)
#  if(NOT "${ARGV0}" IN_LIST as_subproject)
#    _find_package(${ARGV})
#  endif()
#endmacro()


#dependencies
#foreach(package cameo-com-jzmq  )
#  add_subdirectory(cameo-com/${package})
#endforeach()

if(${ZEROMQ_JAVA} STREQUAL "jzmq")
  list(APPEND Java java java/server/jzmq java/console/jzmq)
  message(STATUS "Java ZeroMQ implementation: ${ZEROMQ_JAVA}")
elseif(${ZEROMQ_JAVA} STREQUAL "jeromq")
  list(APPEND Java java java/server/jeromq java/console/jeromq)
  message(STATUS "Java ZeroMQ implementation: ${ZEROMQ_JAVA}")
else()
  message(STATUS "Unknown ZeroMQ implementation: ${ZEROMQ_JAVA}")
endif()

foreach(package ${Java} ${Proxies} ${APIs} ${TestsExamples})
  add_subdirectory(${package})
endforeach()


# common CPack configuration
#include(${CMAKE_SOURCE_DIR}/CPackConfigCommon.in)
#set(CPACK_PROJECT_CONFIG_FILE ${CMAKE_CURRENT_SOURCE_DIR}/packaging/CPackConfig.cmake)
#include(CPack)
