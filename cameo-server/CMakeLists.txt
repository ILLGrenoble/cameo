cmake_minimum_required(VERSION 3.18.0)

project(cameo-server-jzmq
  VERSION 1.0.2
  HOMEPAGE_URL "https://code.ill.fr/cameo/cameo"
  )

include(GNUInstallDirs)

if(NOT DEFINED CMAKE_BUILD_TYPE)
  set(CMAKE_BUILD_TYPE Relase)
endif()

# find the jar file produced by MAVEN
set(PROJECT_TARGET_DIR "${CMAKE_CURRENT_SOURCE_DIR}/target")
set(PROJECT_JAR ${PROJECT_TARGET_DIR}/${PROJECT_NAME}${JAVA_JZMQ}-${PROJECT_VERSION}-full.jar)

set(JAVA_INSTALL_DIR ${CMAKE_INSTALL_DATAROOTDIR}/java/cameo)
file(MAKE_DIRECTORY ${CMAKE_CURRENT_BINARY_DIR}/installed/${JAVA_INSTALL_DIR})


add_custom_command(
  OUTPUT ${PROJECT_JAR}
  COMMAND mvn  clean install
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}
  VERBATIM
  )
add_custom_target(server
  ALL
  DEPENDS ${PROJECT_JAR}
  COMMAND ${CMAKE_COMMAND} -E copy ${PROJECT_JAR} ${CMAKE_CURRENT_BINARY_DIR}/installed/${JAVA_INSTALL_DIR}
  )

if(${UNIX})
if(${CMAKE_SYSTEM_NAME} STREQUAL "Linux")

#add_custom_command(
#    OUTPUT "${CMAKE_CURRENT_BINARY_DIR}/changelog.gz
#    COMMAND gzip -cn9 "${CMAKE_CURRENT_SOURCE_DIR}/changelog" "${CMAKE_CURRENT_SOURCE_DIR}/changelog.gz"

#WORKING_DIRECTORY "${CMAKE_CURRENT_SOURCE_DIR}"
#    DEPENDS "${CMAKE_CURRENT_SOURCE_DIR}/changelog"
#    COMMENT "Compressing changelog"
#)

#add_custom_target(changelog ALL DEPENDS "${CMAKE_CURRENT_BINARY_DIR}/changelog.gz")

#install(FILES "${CMAKE_CURRENT_BINARY_DIR}/changelog.gz"
#    DESTINATION "${CMAKE_INSTALL_DOCDIR}"
#)
endif()
endif(${UNIX})

if(${CMAKE_PROJECT_NAME} STREQUAL ${PROJECT_NAME})
  include(${CMAKE_CURRENT_SOURCE_DIR}/../CPackConfigCommon.in)
  
  set (CPACK_PACKAGE_DESCRIPTION_SUMMARY "Cameo server java")
  set (CPACK_PACKAGE_DESCRIPTION "This is the long description")
endif()

include(${CMAKE_CURRENT_SOURCE_DIR}/CPackConfig.in)

#include(../CPackConfig.cmake.in)

set(CPACK_INSTALLED_DIRECTORIES ${CMAKE_CURRENT_BINARY_DIR}/installed/${JAVA_INSTALL_DIR} usr/${JAVA_INSTALL_DIR})

include(CPack)
