cmake_minimum_required(VERSION 3.18.0)

project(cameo
  VERSION 1.0.2
  LANGUAGES C 
  HOMEPAGE_URL "https://code.ill.fr/cameo/cameo"
  )




#dependencies
set(CPACK_PACKAGE_NAME ${PROJECT_NAME})
foreach(package cameo-com-jzmq  )
  add_subdirectory(cameo-com/${package})
endforeach()


set(CPACK_PACKAGE_NAME ${PROJECT_NAME})
foreach(package  cameo-server-jzmq cameo-console-jzmq cameo-api-cpp)
  add_subdirectory(${package})
endforeach()


# common CPack configuration
include(${CMAKE_SOURCE_DIR}/CPackConfigCommon.in)
include(CPack)

