cmake_minimum_required(VERSION 3.20.0)

project(cameo-examples-jeromq
  VERSION 0.0.0
  HOMEPAGE_URL "https://code.ill.fr/cameo/cameo"
  )

# find the jar file produced by Maven
set(PROJECT_JAR_FILE_NAME ${PROJECT_NAME}${JAVA_JEROMQ}-full.jar)
set(PROJECT_TARGET_DIR "${CMAKE_CURRENT_SOURCE_DIR}/target")
set(PROJECT_JAR ${PROJECT_TARGET_DIR}/${PROJECT_JAR_FILE_NAME})

# Simply build the examples
add_custom_target(examples-jeromq
  ALL
  COMMAND mvn clean package
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}
  )
