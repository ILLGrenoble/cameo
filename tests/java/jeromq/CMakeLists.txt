cmake_minimum_required(VERSION 3.12.0)

project(cameo-tests-jeromq
  VERSION 0.0.0
  HOMEPAGE_URL "https://code.ill.fr/cameo/cameo"
  )

# find the jar file produced by MAVEN
set(PROJECT_JAR_FILE_NAME ${PROJECT_NAME}${JAVA_JEROMQ}-full.jar)
set(PROJECT_TARGET_DIR "${CMAKE_CURRENT_SOURCE_DIR}/target")
set(PROJECT_JAR ${PROJECT_TARGET_DIR}/${PROJECT_JAR_FILE_NAME})

add_custom_command(
  OUTPUT ${PROJECT_JAR}
  COMMAND mvn clean package
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}
  VERBATIM
  )
  
# It is not necessary to copy the jar file.
add_custom_target(tests-jeromq
  ALL
  DEPENDS ${PROJECT_JAR}
  )
