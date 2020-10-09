if(NOT DEFINED PROJECT_NAME)
  cmake_minimum_required(VERSION 3.7.2)

  # Project name and version
  project(cameopy VERSION 1.0.0 LANGUAGES CXX)
endif()

include(GNUInstallDirs)
include(CMakePackageConfigHelpers)

set(BUILD_SHARED_LIBS True)
# this works only on linux....
list(APPEND CMAKE_PREFIX_PATH "${CMAKE_INSTALL_PREFIX}/lib64")

#list(APPEND CMAKE_MODULE_PATH ${pybind11_DIR}/../pybind11/tools/)
#list(APPEND CMAKE_MODULE_PATH ${pybind11_DIR})
#list(APPEND CMAKE_PREFIX_PATH ${pybind11_DIR}/../)
#message(WARNING "CMAKE_MODULE_PATH: ${CMAKE_MODULE_PATH}")
#message(WARNING "CMAKE_ARGS: ${CMAKE_PREFIX_PATH}")
#message(WARNING "PYBIND11_DIR: ${pybind11_DIR}/../")


#------------------------------------------------------------
# Dependencies
#------------------------------------------------------------
message(STATUS ${CMAKE_PREFIX_PATH})
#list(APPEND CMAKE_MODULE_PATH ${CMAKE_CURRENT_SOURCE_DIR})
find_package(cameo 1.1.0 REQUIRED) # provided by the C++ API

#find_package(pybind11 CONFIG)
find_package(pybind11 2.4.3 REQUIRED)

pybind11_add_module(cameopy Application.cpp)

#file(GENERATE OUTPUT /tmp/message.log CONTENT $<TARGET_PROPERTY:cameo,INTERFACE_INCLUDE_DIRECTORIES>)
target_link_libraries(cameopy PRIVATE cameo)
#target_compile_features(cameopy PUBLIC cxx_std_17)
#target_include_directories(cameopy
#  PRIVATE $<TARGET_PROPERTY:cameo,INTERFACE_INCLUDE_DIRECTORIES>
#  )

#target_include_directories(${MODULE_NAME} ${cameo_INCLUDE_DIR})
set_target_properties(cameopy PROPERTIES
  SOVERSION ${PROJECT_VERSION_MAJOR}
  VERSION ${PROJECT_VERSION}
  )



#####------------------------------------------- copied from CMakeLists.txt of openPMD-api
if(WIN32)
  set(CMAKE_INSTALL_PYTHONDIR_DEFAULT
    "${CMAKE_INSTALL_LIBDIR}/site-packages"
    )
else()
  set(CMAKE_INSTALL_PYTHONDIR_DEFAULT
    "${CMAKE_INSTALL_LIBDIR}/python${PYTHON_VERSION_MAJOR}.${PYTHON_VERSION_MINOR}/site-packages"
    )
endif()
set(CMAKE_INSTALL_PYTHONDIR "${CMAKE_INSTALL_PYTHONDIR_DEFAULT}"
  CACHE STRING "Location for installed python package"
  )
#set(CMAKE_PYTHON_OUTPUT_DIRECTORY
#  "${openPMD_BINARY_DIR}/${CMAKE_INSTALL_PYTHONDIR}"
#  CACHE PATH "Build directory for python modules"
#  )
# set_target_properties(raytracing.py PROPERTIES
# #  ARCHIVE_OUTPUT_NAME openpmd_api_cxx
# #  LIBRARY_OUTPUT_NAME openpmd_api_cxx
#   ARCHIVE_OUTPUT_DIRECTORY ${CMAKE_PYTHON_OUTPUT_DIRECTORY}/raytracing_api
#   LIBRARY_OUTPUT_DIRECTORY ${CMAKE_PYTHON_OUTPUT_DIRECTORY}/raytracing_api
#   RUNTIME_OUTPUT_DIRECTORY ${CMAKE_PYTHON_OUTPUT_DIRECTORY}/raytracing_api
#   PDB_OUTPUT_DIRECTORY ${CMAKE_PYTHON_OUTPUT_DIRECTORY}/raytracing_api_api
#   COMPILE_PDB_OUTPUT_DIRECTORY ${CMAKE_PYTHON_OUTPUT_DIRECTORY}/raytracing_api
#   )
# add_custom_command(TARGET raytracing.py POST_BUILD
#   COMMAND ${CMAKE_COMMAND} -E copy_directory
#   ${openPMD_SOURCE_DIR}/python/src/raytracing_api
#   ${CMAKE_PYTHON_OUTPUT_DIRECTORY}/raytracing_api
#   )

##### End of ------------------------------------ copied from CMakeLists.txt of openPMD-api



#---------------
# Library version
write_basic_package_version_file(${PROJECT_NAME}ConfigVersion.cmake
  COMPATIBILITY SameMajorVersion
  VERSION ${PROJECT_VERSION}
  )
install(FILES ${CMAKE_CURRENT_BINARY_DIR}/${PROJECT_NAME}ConfigVersion.cmake
  DESTINATION ${CMAKE_INSTALL_LIBDIR}/cmake/${PROJECT_NAME})

#------------------------------------------------------------
# Install
#------------------------------------------------------------
#------------------------------------------------------------
# Install
#------------------------------------------------------------
install(
  FILES   ${CMAKE_CURRENT_SOURCE_DIR}/__init__.py
  DESTINATION ${CMAKE_INSTALL_PYTHONDIR}/${PROJECT_NAME}
  )
install(TARGETS cameopy
  EXPORT cameopyexport
  DESTINATION ${CMAKE_INSTALL_PYTHONDIR}/${PROJECT_NAME}
  )

