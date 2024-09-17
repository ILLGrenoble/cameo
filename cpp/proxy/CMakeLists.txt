#------------------------------------------------------------
# Project
#------------------------------------------------------------

cmake_minimum_required(VERSION 3.20.0)
  
# Project name and version
project(cameo-proxy
  VERSION 2.0.1
  LANGUAGES CXX
  HOMEPAGE_URL "https://code.ill.fr/cameo/cameo"
  )

include(GNUInstallDirs)
include(CMakePackageConfigHelpers)
include(FetchContent)

# name of the target library
set(LIBNAME ${PROJECT_NAME})

list(APPEND CMAKE_MODULE_PATH ${CMAKE_CURRENT_SOURCE_DIR}/../cmake)
include(AddZeroMQLibrary)
add_zeromq_library()

#------------------------------------------------------------
# Programs
#------------------------------------------------------------

add_executable(cameo-rep-proxy src/RepProxy.cpp)
target_link_libraries(cameo-rep-proxy PRIVATE libzmq)

add_executable(cameo-pub-proxy src/PubProxy.cpp)
target_link_libraries(cameo-pub-proxy PRIVATE libzmq)

install(PROGRAMS ${CMAKE_CURRENT_BINARY_DIR}/cameo-rep-proxy ${CMAKE_CURRENT_BINARY_DIR}/cameo-pub-proxy 
        TYPE BIN
)

#------------------------------------------------------------
# CPack
#------------------------------------------------------------
include(${CMAKE_CURRENT_SOURCE_DIR}/packaging/CPackConfig.cmake)
include(CPack)
