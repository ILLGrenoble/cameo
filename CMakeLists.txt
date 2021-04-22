cmake_minimum_required(VERSION 3.12.0)

project(cameo
  VERSION 1.0.2
  LANGUAGES C 
  HOMEPAGE_URL "https://code.ill.fr/cameo/cameo"
  )

#------------------------------------------------------------
# This is a trick: for the subprojects metioned here,
# the find_package does nothing, since the targets will be defined
# by the add_subdirectory command
set(as_subproject cameo-api-cpp-static)
macro(find_package)
  if(NOT "${ARGV0}" IN_LIST as_subproject)
    _find_package(${ARGV})
  endif()
endmacro()


#dependencies
foreach(package cameo-com-jzmq  )
  add_subdirectory(cameo-com/${package})
endforeach()

foreach(package  #cameo-server-jzmq cameo-console-jzmq
    cameo-api-cpp cameo-api-python)
  add_subdirectory(${package})
endforeach()


# common CPack configuration
#include(${CMAKE_SOURCE_DIR}/CPackConfigCommon.in)
#set(CPACK_PROJECT_CONFIG_FILE ${CMAKE_CURRENT_SOURCE_DIR}/packaging/CPackConfig.cmake)
#include(CPack)
