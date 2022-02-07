cmake_minimum_required(VERSION 3.12.0)

project(cameo-console-jzmq
  VERSION 2.0.0
  HOMEPAGE_URL "https://code.ill.fr/cameo/cameo"
  )

include(GNUInstallDirs)

# find the jar file produced by MAVEN
set(PROJECT_JAR_FILE_NAME ${PROJECT_NAME}${JAVA_JZMQ}-${PROJECT_VERSION}-full.jar)
set(PROJECT_TARGET_DIR "${CMAKE_CURRENT_SOURCE_DIR}/target")
set(PROJECT_JAR ${PROJECT_TARGET_DIR}/${PROJECT_JAR_FILE_NAME})

set(JAVA_INSTALL_DIR ${CMAKE_INSTALL_DATAROOTDIR}/java/cameo)
file(MAKE_DIRECTORY ${CMAKE_CURRENT_BINARY_DIR}/installed/${JAVA_INSTALL_DIR})


add_custom_command(
  OUTPUT ${PROJECT_JAR}
  COMMAND mvn clean install
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}
  VERBATIM
  )
add_custom_target(console-jzmq
  ALL
  DEPENDS ${PROJECT_JAR}
  COMMAND ${CMAKE_COMMAND} -E copy ${PROJECT_JAR} ${CMAKE_CURRENT_BINARY_DIR}/installed/${JAVA_INSTALL_DIR}
  )

install(DIRECTORY ${CMAKE_CURRENT_BINARY_DIR}/installed/${JAVA_INSTALL_DIR}/
  DESTINATION ${JAVA_INSTALL_DIR}
#  COMPONENT console
  )

configure_file(${CMAKE_CURRENT_SOURCE_DIR}/../console/scripts/cmo.in
  ${CMAKE_CURRENT_BINARY_DIR}/cmo)

install(PROGRAMS ${CMAKE_CURRENT_BINARY_DIR}/cmo
  TYPE BIN
  )

#------------------------------------------------------------
# CPack
if(${CMAKE_PROJECT_NAME} STREQUAL ${PROJECT_NAME})
  include(${CMAKE_CURRENT_SOURCE_DIR}/../CPackConfigCommon.in)
  include(${CMAKE_CURRENT_SOURCE_DIR}/CPackConfig.in)
  include(CPack)
endif()
#------------------------------------------------------------
