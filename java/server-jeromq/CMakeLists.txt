cmake_minimum_required(VERSION 3.20.0)

project(cameo-server-jeromq
  VERSION 3.0.0
  HOMEPAGE_URL "https://code.ill.fr/cameo/cameo"
  )

include(GNUInstallDirs)

# Find the jar file produced by Maven
set(PROJECT_JAR_FILE_NAME ${PROJECT_NAME}${JAVA_JEROMQ}-${PROJECT_VERSION}-full.jar)
set(PROJECT_TARGET_DIR "${CMAKE_CURRENT_SOURCE_DIR}/target")
set(PROJECT_JAR ${PROJECT_TARGET_DIR}/${PROJECT_JAR_FILE_NAME})

set(JAVA_INSTALL_DIR ${CMAKE_INSTALL_DATAROOTDIR}/java/cameo)
file(MAKE_DIRECTORY ${CMAKE_CURRENT_BINARY_DIR}/installed/${JAVA_INSTALL_DIR})

# Copy the jar generated in java directory
add_custom_target(server-jeromq
  ALL
  DEPENDS ${PROJECT_JAR}
  COMMAND ${CMAKE_COMMAND} -E copy ${PROJECT_JAR} ${CMAKE_CURRENT_BINARY_DIR}/installed/${JAVA_INSTALL_DIR}
  )

install(DIRECTORY ${CMAKE_CURRENT_BINARY_DIR}/installed/${JAVA_INSTALL_DIR}/
  DESTINATION ${JAVA_INSTALL_DIR}
  )

configure_file(${CMAKE_CURRENT_SOURCE_DIR}/scripts/cameo-server.in
  ${CMAKE_CURRENT_BINARY_DIR}/cameo-server)

install(PROGRAMS ${CMAKE_CURRENT_BINARY_DIR}/cameo-server
  TYPE BIN
  )


#------------------------------------------------------------
# CPack
#------------------------------------------------------------

if(${CMAKE_PROJECT_NAME} STREQUAL ${PROJECT_NAME})
  include(${CMAKE_CURRENT_SOURCE_DIR}/../../CPackConfigCommon.in)
  include(${CMAKE_CURRENT_SOURCE_DIR}/CPackConfig.in)
  include(CPack)
endif()
