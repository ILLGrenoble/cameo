cmake_minimum_required(VERSION 3.18.0)

project(cameo-com-jzmq
  VERSION 0.0.4
  HOMEPAGE_URL "https://code.ill.fr/cameo/cameo"
  )

# find the jar file produced by MAVEN
set(PROJECT_JAR ${PROJECT_TARGET_DIR}/${PROJECT_NAME}${JAVA_JZMQ}-${PROJECT_VERSION}-full.jar)

add_custom_command(
  OUTPUT ${PROJECT_JAR}
  COMMAND mvn  install
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}
  VERBATIM
  )
add_custom_target(${PROJECT_NAME}
#  ALL
  DEPENDS ${PROJECT_JAR}
  )


