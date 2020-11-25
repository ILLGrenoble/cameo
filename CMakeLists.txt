if(NOT DEFINED PROJECT_NAME)
  cmake_minimum_required(VERSION 3.7.2)
  
  # Project name and version
  project(cameo VERSION 1.0.2 LANGUAGES CXX)
  #cmake_policy(SET CMP0048 NEW)
endif()

message("========================================")
message("Project ${PROJECT_NAME}, major version: ${PROJECT_VERSION_MAJOR}, version ${PROJECT_VERSION}")

include(GNUInstallDirs)
include(CMakePackageConfigHelpers)
include(FetchContent)

if(NOT DEFINED BUILD_SHARED_LIBS)
  option(BUILD_SHARED_LIBS "Building dynamic (ON) or static (OFF)" ON)
  #set(BUILD_SHARED_LIBS True)
endif()

if(NOT DEFINED CMAKE_CXX_STANDARD)
  # specify the C++ standard
  set(CMAKE_CXX_STANDARD 17)
  set(CMAKE_CXX_STANDARD_REQUIRED True)
endif()


# fix the RPATH for the linker
#if(not APPLE)
#  set(CMAKE_INSTALL_RPATH $ORIGIN)
#endif()

#------------------------------------------------------------
# Dependencies
#------------------------------------------------------------
list(APPEND CMAKE_MODULE_PATH ${CMAKE_CURRENT_SOURCE_DIR})
find_package(ZeroMQ REQUIRED)
find_package(Rapidjson QUIET)
if(NOT Rapidjson_FOUND)
  message(WARNING "Using Rapidjson from GIT repository")
  FetchContent_Declare(rapidjson
	GIT_REPOSITORY https://github.com/Tencent/rapidjson.git
	GIT_TAG v1.1.0
	GIT_SHALLOW True
	#INSTALL_COMMAND "make install"
	)
  FetchContent_GetProperties(rapidjson)
  FetchContent_Populate(rapidjson)

  set(Rapidjson_ROOT ${rapidjson_SOURCE_DIR})
  find_package(Rapidjson REQUIRED)
endif()
  message(${Rapidjson_INCLUDE_DIRS})
#------------------------------------------------------------
# Libraries
#------------------------------------------------------------

#---------------
# Files
file(GLOB_RECURSE CAMEO_SOURCE_FILES "src/*.cpp")

add_library(cameo ${CAMEO_SOURCE_FILES})
target_link_libraries(cameo PRIVATE zmq)
target_include_directories(cameo
  PRIVATE ${CMAKE_CURRENT_SOURCE_DIR}/src/ # all the private headers
  PUBLIC $<BUILD_INTERFACE:${CMAKE_CURRENT_SOURCE_DIR}/include> 
  PUBLIC $<INSTALL_INTERFACE:${CMAKE_INSTALL_INCLUDEDIR}>
  )
target_include_directories(cameo
    SYSTEM PRIVATE ${Rapidjson_INCLUDE_DIRS}/
)
#target_include_directories(cameo PRIVATE ${DOCTEST_INCLUDE_DIR})
#target_compile_features(cameo PUBLIC cxx_std_11)

set_target_properties(cameo PROPERTIES
  SOVERSION ${PROJECT_VERSION_MAJOR}
  VERSION ${PROJECT_VERSION}
  )

#---------------
# Library version
write_basic_package_version_file(cameoConfigVersion.cmake
  COMPATIBILITY SameMajorVersion
  VERSION ${PROJECT_VERSION}
  )

#------------------------------------------------------------
# Tests
#------------------------------------------------------------

# Note: execute ctest --verbose to have more details 
enable_testing()
add_subdirectory(tests)

#------------------------------------------------------------
# Install
#------------------------------------------------------------
install(FILES ${CMAKE_CURRENT_BINARY_DIR}/cameoConfigVersion.cmake
  DESTINATION ${CMAKE_INSTALL_LIBDIR}/cmake)

# this is to install the headers
install(DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/include/ # don't forget the trailing /
  DESTINATION ${CMAKE_INSTALL_INCLUDEDIR}/${PROJECT_NAME}
  )

# in CMake 3.14 you just need #install(TARGETS cameo)
install(TARGETS cameo
  EXPORT cameoexport
  RUNTIME DESTINATION ${CMAKE_INSTALL_BINDIR} # windows
    COMPONENT cameo_Runtime
  LIBRARY DESTINATION ${CMAKE_INSTALL_LIBDIR} # dynamic
    COMPONENT cameo_Runtime
#    NAMELINK_COMPONENT cameo_Development #from CMake 3.12
  ARCHIVE DESTINATION ${CMAKE_INSTALL_LIBDIR} # static
    COMPONENT cameo_Development
  PUBLIC_HEADER  DESTINATION ${CMAKE_INSTALL_INCLUDEDIR}
  INCLUDES DESTINATION ${CMAKE_INSTALL_INCLUDEDIR}
  )


# This makes the project importable from the install directory
# Put config file in per-project dir (name MUST match), can also
# just go into 'cmake'.
install(EXPORT cameoexport
  DESTINATION ${CMAKE_INSTALL_LIBDIR}/cmake
  FILE ${PROJECT_NAME}Config.cmake 
  )
# makes the project importable from the build directory
export(TARGETS cameo FILE ${CMAKE_BINARY_DIR}/${PROJECT_NAME}Config.cmake)

# the install_manifest file is created only after the installation....
#install(FILES ${CMAKE_CURRENT_BINARY_DIR}/install_manifest.txt
#  DESTINATION ${CMAKE_INSTALL_LIBDIR}/cmake
#  RENAME cameo_install_manifest.txt
#  )


#enable_testing()
#add_subdirectory(test)

# include(ExternalProject)
# find_package(Git REQUIRED)

# ExternalProject_Add(
#     doctest
#     PREFIX ${CMAKE_BINARY_DIR}/doctest
#     GIT_REPOSITORY https://github.com/onqtam/doctest.git
#     TIMEOUT 10
#     UPDATE_COMMAND ${GIT_EXECUTABLE} pull
#     CONFIGURE_COMMAND ""
#     BUILD_COMMAND ""
#     INSTALL_COMMAND ""
#     LOG_DOWNLOAD ON
# )

# # Expose required variable (DOCTEST_INCLUDE_DIR) to parent scope
# ExternalProject_Get_Property(doctest source_dir)
# set(DOCTEST_INCLUDE_DIR ${source_dir}/doctest CACHE INTERNAL "Path to include folder for doctest")

#include(CMakeFindDependencyMacro)
#find_dependency(
#include("${CMAKE_CURRENT_LIST_DIR}/cameo.cmake")
################ Doc
find_package(Doxygen)
if (DOXYGEN_FOUND)
  # set input and output files
  set(DOXYGEN_IN ${CMAKE_CURRENT_SOURCE_DIR}/Doxyfile.in)
  set(DOXYGEN_OUT ${CMAKE_CURRENT_BINARY_DIR}/Doxyfile)
  
  # request to configure the file
  configure_file(${DOXYGEN_IN} ${DOXYGEN_OUT} @ONLY)
  message("Doxygen build started")
  
  # note the option ALL which allows to build the docs together with the application
  add_custom_target( cameo_doc 
    COMMAND ${DOXYGEN_EXECUTABLE} ${DOXYGEN_OUT}
    WORKING_DIRECTORY ${CMAKE_CURRENT_BINARY_DIR}
    COMMENT "Generating API documentation with Doxygen"
    VERBATIM )
else (DOXYGEN_FOUND)
  message("Doxygen need to be installed to generate the doxygen documentation")
endif (DOXYGEN_FOUND)


