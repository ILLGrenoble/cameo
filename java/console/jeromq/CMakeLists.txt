cmake_minimum_required(VERSION 3.20.0)

project(cameo-console-jeromq
  VERSION 2.2.0
  HOMEPAGE_URL "https://code.ill.fr/cameo/cameo"
  )

include(GNUInstallDirs)

# find the jar file produced by Maven
set(PROJECT_JAR_FILE_NAME ${PROJECT_NAME}${JAVA_JZMQ}-${PROJECT_VERSION}-full.jar)
set(PROJECT_TARGET_DIR "${CMAKE_CURRENT_SOURCE_DIR}/target")
set(PROJECT_JAR ${PROJECT_TARGET_DIR}/${PROJECT_JAR_FILE_NAME})

set(JAVA_INSTALL_DIR ${CMAKE_INSTALL_DATAROOTDIR}/java/cameo)
file(MAKE_DIRECTORY ${CMAKE_CURRENT_BINARY_DIR}/installed/${JAVA_INSTALL_DIR})

# copy the jar generated in java directory
add_custom_target(console-jeromq
  ALL
  DEPENDS ${PROJECT_JAR}
  COMMAND ${CMAKE_COMMAND} -E copy ${PROJECT_JAR} ${CMAKE_CURRENT_BINARY_DIR}/installed/${JAVA_INSTALL_DIR}
  )

install(DIRECTORY ${CMAKE_CURRENT_BINARY_DIR}/installed/${JAVA_INSTALL_DIR}/
  DESTINATION ${JAVA_INSTALL_DIR}
#  COMPONENT console
  )
