cmake_minimum_required(VERSION 3.20.0)

project(cameo-java
  VERSION 2.2.0
  HOMEPAGE_URL "https://code.ill.fr/cameo/cameo"
  )

include(GNUInstallDirs)

# build all the Java modules
# the command is called twice: during build and install
# do not clean, to avoid useless recompilation
add_custom_target(cameo-java
  ALL
  COMMAND mvn install
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}
  )
