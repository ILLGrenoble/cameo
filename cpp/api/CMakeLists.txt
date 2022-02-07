cmake_minimum_required(VERSION 3.12.0)

project(cameo-api-cpp
  VERSION 2.0.0
  LANGUAGES CXX
  HOMEPAGE_URL "https://code.ill.fr/cameo/cameo"
  )
#--------------- Helpers
include(GNUInstallDirs)
include(CMakePackageConfigHelpers)
include(FetchContent)

# name of the target library
set(LIBNAME ${PROJECT_NAME})
set(NAMESPACE Cameo)
set(INSTALL_INCLUDEDIR ${CMAKE_INSTALL_INCLUDEDIR}/cameo/api)
set(ConfigPackageLocation ${CMAKE_INSTALL_DATADIR}/cmake/${PROJECT_NAME})
set(component_runtime CAMEO_API_CPP_RUNTIME)
set(component_development CAMEO_API_CPP_DEVELOPMENT)
#--------------- Options
option(CAMEO_API_CPP_TESTS "Compiling the test programs" OFF)
option(BUILD_SHARED_LIBS "Compiling shared or static library version of the api" ON)

if(NOT DEFINED ${CMAKE_BUILD_TYPE})
  set(CMAKE_BUILD_TYPE "Release") # set Release by default
endif()
message(STATUS "Build type: ${CMAKE_BUILD_TYPE}")

# fix the RPATH for the linker
if(NOT APPLE)
  set(CMAKE_INSTALL_RPATH $ORIGIN)
endif()

#------------------------------------------------------------
# Dependencies
#------------------------------------------------------------
# this is needed when including this file as subdir
list(APPEND CMAKE_MODULE_PATH ${CMAKE_CURRENT_SOURCE_DIR})

find_package(ZeroMQ REQUIRED)
find_package (Threads REQUIRED)
find_package(Rapidjson 1.1.0 QUIET)
if(NOT Rapidjson_FOUND)
  message(WARNING "Using Rapidjson from GIT repository")
  FetchContent_Declare(rapidjson
	GIT_REPOSITORY https://github.com/Tencent/rapidjson.git
	GIT_TAG v1.1.0
	GIT_SHALLOW True
	)
  FetchContent_GetProperties(rapidjson)
  FetchContent_Populate(rapidjson)

  set(Rapidjson_ROOT ${rapidjson_SOURCE_DIR})
  find_package(Rapidjson REQUIRED)
endif()
message(STATUS "Rapidjson found here: ${Rapidjson_INCLUDE_DIRS}")


#------------------------------------------------------------
#------------------------------------------------------------
# Libraries
#------------------------------------------------------------
#---------------
# Files
file(GLOB_RECURSE CAMEO_SOURCE_FILES "${CMAKE_CURRENT_SOURCE_DIR}/src/*.cpp")
configure_file(include/cameo.h.in ${INSTALL_INCLUDEDIR}/cameo.h )

add_library(${LIBNAME}-obj OBJECT ${CAMEO_SOURCE_FILES})
target_link_libraries(${LIBNAME}-obj PUBLIC zmq Threads::Threads) # for some reason this is not propagated to ${LIBNAME}
target_include_directories(${LIBNAME}-obj
  PRIVATE ${CMAKE_CURRENT_SOURCE_DIR}/src/ # all the private headers
  PUBLIC $<BUILD_INTERFACE:${CMAKE_CURRENT_SOURCE_DIR}/include>
  PUBLIC $<BUILD_INTERFACE:${CMAKE_CURRENT_BINARY_DIR}/include>  # here's the version
  PUBLIC $<INSTALL_INTERFACE:${CMAKE_INSTALL_INCLUDEDIR}>
  )
target_include_directories(${LIBNAME}-obj
  SYSTEM PRIVATE ${Rapidjson_INCLUDE_DIRS}/
)
target_compile_features(${LIBNAME}-obj PUBLIC cxx_std_17)

set_target_properties(${LIBNAME}-obj PROPERTIES
  SOVERSION ${PROJECT_VERSION_MAJOR}
  VERSION ${PROJECT_VERSION}
  POSITION_INDEPENDENT_CODE 1 # this is needed for static compilation for cameo-api-python
  )

add_library(${LIBNAME}-static STATIC $<TARGET_OBJECTS:${LIBNAME}-obj>)
add_library(${LIBNAME} $<TARGET_OBJECTS:${LIBNAME}-obj>)

add_library(${NAMESPACE}::${LIBNAME} ALIAS ${LIBNAME})
add_library(${NAMESPACE}::${LIBNAME}-obj ALIAS ${LIBNAME}-obj)
add_library(${NAMESPACE}::${LIBNAME}-static ALIAS ${LIBNAME}-static)

target_include_directories(${LIBNAME}
  SYSTEM PRIVATE ${Rapidjson_INCLUDE_DIRS}/
  )

get_target_property(obj_INTERFACE_LINK_LIBRARIES ${LIBNAME}-obj INTERFACE_LINK_LIBRARIES)
get_target_property(obj_INTERFACE_COMPILE_FEATURES ${LIBNAME}-obj INTERFACE_COMPILE_FEATURES)
get_target_property(obj_INTERFACE_INCLUDE_DIRECTORIES ${LIBNAME}-obj INTERFACE_INCLUDE_DIRECTORIES)
set_target_properties(${LIBNAME} PROPERTIES
  SOVERSION ${PROJECT_VERSION_MAJOR}
  VERSION ${PROJECT_VERSION}
  POSITION_INDEPENDENT_CODE 1 # this is needed for static compilation for cameo-api-python
  INTERFACE_LINK_LIBRARIES "${obj_INTERFACE_LINK_LIBRARIES}"
  INTERFACE_COMPILE_FEATURES ${obj_INTERFACE_COMPILE_FEATURES}
  INTERFACE_INCLUDE_DIRECTORIES "${obj_INTERFACE_INCLUDE_DIRECTORIES}"
  )
set_target_properties(${LIBNAME}-static PROPERTIES
  INTERFACE_LINK_LIBRARIES "${obj_INTERFACE_LINK_LIBRARIES}"
  INTERFACE_COMPILE_FEATURES "${obj_INTERFACE_COMPILE_FEATURES}"
  INTERFACE_INCLUDE_DIRECTORIES "${obj_INTERFACE_INCLUDE_DIRECTORIES}"
  )

#------------------------------------------------------------

include(GenerateExportHeader)
generate_export_header(${LIBNAME}
    EXPORT_MACRO_NAME ${LIBNAME}_EXPORT
    EXPORT_FILE_NAME ${CMAKE_CURRENT_BINARY_DIR}/${INSTALL_INCLUDEDIR}/export.h
)

#------------------------------------------------------------
# Install
#------------------------------------------------------------
install(FILES ${CMAKE_CURRENT_SOURCE_DIR}/LICENSE.txt
  DESTINATION ${CMAKE_INSTALL_DOCDIR}/
  RENAME copyright
  COMPONENT ${component_runtime}
  )



#---------------
# Library version
write_basic_package_version_file(${PROJECT_NAME}-config-version.cmake
  COMPATIBILITY SameMajorVersion
  VERSION ${PROJECT_VERSION}
  )
write_basic_package_version_file(${PROJECT_NAME}-static-config-version.cmake
  COMPATIBILITY SameMajorVersion
  VERSION ${PROJECT_VERSION}
  )

install(TARGETS ${LIBNAME} 
  EXPORT cameoTargets
  RUNTIME DESTINATION ${CMAKE_INSTALL_BINDIR} # windows
    COMPONENT ${component_runtime}
  LIBRARY DESTINATION ${CMAKE_INSTALL_LIBDIR}/ # dynamic
    COMPONENT ${component_runtime}
    NAMELINK_COMPONENT ${component_development} #from CMake 3.12
  ARCHIVE DESTINATION ${CMAKE_INSTALL_LIBDIR}/ # static
    COMPONENT ${component_development}
  PUBLIC_HEADER  DESTINATION ${INSTALL_INCLUDEDIR}
    COMPONENT ${component_development}
#  INCLUDES DESTINATION ${CMAKE_INSTALL_INCLUDEDIR}
  )

# this is to install the headers
install(DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/include/ # don't forget the trailing /
  DESTINATION ${INSTALL_INCLUDEDIR}
  COMPONENT ${component_development}
  FILES_MATCHING  PATTERN *.h
  )
# this is to install the version header that is generated by cmake
install(DIRECTORY ${CMAKE_CURRENT_BINARY_DIR}/include/ # don't forget the trailing /
  DESTINATION ${CMAKE_INSTALL_INCLUDEDIR}
  COMPONENT ${component_development}
  FILES_MATCHING  PATTERN *.h
  )

# this is for cameo-api-python to compile from the local build tree
export(TARGETS ${LIBNAME}-static
  NAMESPACE ${NAMESPACE}::
  FILE ${CMAKE_CURRENT_BINARY_DIR}/${PROJECT_NAME}-static-targets.cmake
  )
export(EXPORT cameoTargets
  #   FILE ${CMAKE_CURRENT_BINARY_DIR}/${PROJECT_NAME}-${type}-targets.cmake
  FILE ${CMAKE_CURRENT_BINARY_DIR}/${PROJECT_NAME}-targets.cmake
  NAMESPACE ${NAMESPACE}::
  )
configure_package_config_file(cmake/${PROJECT_NAME}-config.cmake.in
  ${CMAKE_CURRENT_BINARY_DIR}/${PROJECT_NAME}-config.cmake
  INSTALL_DESTINATION  ${ConfigPackageLocation}
  )
configure_file(cmake/${PROJECT_NAME}-static-config.cmake.in
  ${CMAKE_CURRENT_BINARY_DIR}/${PROJECT_NAME}-static-config.cmake
  COPYONLY
  )


install(EXPORT cameoTargets
  FILE ${PROJECT_NAME}-targets.cmake
#  FILE ${PROJECT_NAME}Config.cmake #${PROJECT_NAME}Targets.cmake
  NAMESPACE ${NAMESPACE}::
  DESTINATION ${ConfigPackageLocation}
  COMPONENT ${component_development}
  )
install(
  FILES
  ${CMAKE_CURRENT_BINARY_DIR}/${PROJECT_NAME}-config.cmake
  ${CMAKE_CURRENT_BINARY_DIR}/${PROJECT_NAME}-config-version.cmake
  ${CMAKE_CURRENT_SOURCE_DIR}/FindZeroMQ.cmake
  DESTINATION ${ConfigPackageLocation}
  COMPONENT ${component_development}
  )
# This makes the project importable from the install directory
# Put config file in per-project dir (name MUST match), can also
# just go into 'cmake'.


if(UNIX)
  add_custom_command(
    OUTPUT ${CMAKE_CURRENT_BINARY_DIR}/changelog.gz
    COMMAND gzip -cn9 ${CMAKE_CURRENT_SOURCE_DIR}/ChangeLog ${CMAKE_CURRENT_BINARY_DIR}/changelog.gz
    WORKING_DIRECTORY "${CMAKE_CURRENT_SOURCE_DIR}"
    DEPENDS "${CMAKE_CURRENT_SOURCE_DIR}/changelog"
    COMMENT "Compressing changelog"
	)
  
  add_custom_target(changelog
	ALL
	#	DEPENDS ${CMAKE_CURRENT_BINARY_DIR}/changelog.gz
	COMMAND gzip -cn9 ${CMAKE_CURRENT_SOURCE_DIR}/ChangeLog > ${CMAKE_CURRENT_BINARY_DIR}/changelog.gz
	)
  
  install(FILES "${CMAKE_CURRENT_BINARY_DIR}/changelog.gz"
	DESTINATION "${CMAKE_INSTALL_DOCDIR}"
	COMPONENT ${component_runtime}
	)
endif(UNIX)

#------------------------------------------------------------
# Doc
#------------------------------------------------------------
find_package(Doxygen QUIET)
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

#------------------------------------------------------------
# CPack
#------------------------------------------------------------
include(${CMAKE_CURRENT_SOURCE_DIR}/packaging/CPackConfig.cmake)
include(CPack)

#------------------------------------------------------------
# Tests
#------------------------------------------------------------
if(CAMEO_API_CPP_TESTS)
# Note: execute ctest --verbose to have more details 
enable_testing()
add_subdirectory(tests)
endif(CAMEO_API_CPP_TESTS)
