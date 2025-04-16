cmake_minimum_required(VERSION 3.20.0)

project(cameo
  VERSION 3.0.2
  LANGUAGES C 
  HOMEPAGE_URL "https://code.ill.fr/cameo/cameo"
  )

option(CAMEO_ALL "Compile all the components including tests and examples" OFF)
option(CAMEO_JAVA "Compile Java server, console and API" OFF)
option(CAMEO_PROXIES "Compile proxies" OFF)
option(CAMEO_API_CPP "Compile and install C++ api" OFF)
option(CAMEO_API_PYTHON "Compile and install Python api" OFF)
set(ZEROMQ_JAVA "jzmq" CACHE STRING "ZeroMQ Java implementation (jzmq/jeromq)")
option(CAMEO_DOC "Build the doc" OFF)

# Force Release by default
if(NOT CMAKE_BUILD_TYPE)
  set(CMAKE_BUILD_TYPE "Release")
endif()
message(STATUS "Build type: ${CMAKE_BUILD_TYPE}")

# Java
if(${CAMEO_ALL} OR ${CAMEO_JAVA})
  if(${ZEROMQ_JAVA} STREQUAL "jzmq")
    list(APPEND Java java java/server-jzmq java/console-jzmq)
    message(STATUS "Java ZeroMQ implementation: ${ZEROMQ_JAVA}")
  elseif(${ZEROMQ_JAVA} STREQUAL "jeromq")
    list(APPEND Java java java/server-jeromq java/console-jeromq)
    message(STATUS "Java ZeroMQ implementation: ${ZEROMQ_JAVA}")
  else()
    message(STATUS "Unknown ZeroMQ implementation: ${ZEROMQ_JAVA}")
  endif()
endif()

# Proxies
if(${CAMEO_ALL} OR ${CAMEO_PROXIES})
  list(APPEND Proxies cpp/proxy)
endif()

# C++ API
if(${CAMEO_ALL} OR ${CAMEO_API_CPP})
  list(APPEND APIs cpp/api)
endif()

# Python API
if(${CAMEO_ALL} OR ${CAMEO_API_PYTHON})
  list(APPEND APIs cpp/api)
  mark_as_advanced(CAMEO_API_CPP)
  list(APPEND APIs python/api)
endif()
list(REMOVE_DUPLICATES APIs)

# Tests and examples
if(${CAMEO_ALL})
  if(${ZEROMQ_JAVA} STREQUAL "jzmq")
    list(APPEND TestsExamples tests/java/jzmq)
  elseif(${ZEROMQ_JAVA} STREQUAL "jeromq")
    list(APPEND TestsExamples tests/java/jeromq)
  endif()
  list(APPEND TestsExamples tests/cpp)

  if(${ZEROMQ_JAVA} STREQUAL "jzmq")
    list(APPEND TestsExamples examples/java/jzmq)
  elseif(${ZEROMQ_JAVA} STREQUAL "jeromq")
    list(APPEND TestsExamples examples/java/jeromq)
  endif()
  list(APPEND TestsExamples examples/cpp)
endif()


# Walk through subdirectories
foreach(package ${Java} ${Proxies} ${APIs} ${TestsExamples})
  add_subdirectory(${package})
endforeach()

# Build the doc
if(${CAMEO_DOC})
  add_custom_target(doc-site
    ALL
    COMMAND mkdocs build -d ${CMAKE_CURRENT_BINARY_DIR}/docs
    WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/mkdocs
  )
endif()
