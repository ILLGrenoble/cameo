cmake_minimum_required(VERSION 3.20.0)

# Project name and version
project(cameopy
  VERSION 3.0.0
  LANGUAGES CXX
  HOMEPAGE_URL "https://code.ill.fr/cameo/cameo"
  )

include(GNUInstallDirs)
include(CMakePackageConfigHelpers)
include(FetchContent)


# In order to find AddZeroMQLibrary.cmake
list(APPEND CMAKE_MODULE_PATH ${CMAKE_CURRENT_SOURCE_DIR}/../../cpp/cmake/)
include(AddZeroMQLibrary)
add_zeromq_library()


#------------------------------------------------------------
# Dependencies
#------------------------------------------------------------

if (NOT TARGET cameo-api-cpp-static)
  find_package(cameo-api-cpp-static 3.0 REQUIRED) # provided by the C++ API
endif()

find_package(Python COMPONENTS Interpreter Development)
find_package(pybind11 2.9 CONFIG)

if(NOT pybind11_FOUND)
  message(STATUS "Using pybind11 from GIT repository")
  FetchContent_Declare(pybind11
	GIT_REPOSITORY https://github.com/pybind/pybind11.git
	GIT_TAG v2.9.2
	GIT_SHALLOW True
	)
  FetchContent_GetProperties(pybind11)
  if(NOT pybind11_POPULATED)
	FetchContent_Populate(pybind11)
	add_subdirectory(${pybind11_SOURCE_DIR} ${pybind11_BINARY_DIR})
	mark_as_advanced(PYBIND11_TEST)
	mark_as_advanced(PYBIND11_INSTALL)
  endif()
  #else()
#  find_package(pybind11 2.4.3 REQUIRED)
endif()

pybind11_add_module(cameopy src/Application.cpp)
target_include_directories(cameopy SYSTEM PRIVATE ${RapidJSON_INCLUDE_DIRS})

target_link_libraries(cameopy PRIVATE Cameo::cameo-api-cpp-static)

set_target_properties(cameopy PROPERTIES
  SOVERSION ${PROJECT_VERSION_MAJOR}
  VERSION ${PROJECT_VERSION}
  )


# Copied from CMakeLists.txt of openPMD-api
if(WIN32)
  set(CMAKE_INSTALL_PYTHONDIR_DEFAULT
    "${CMAKE_INSTALL_LIBDIR}/site-packages"
    )
else()
  file(RELATIVE_PATH CMAKE_INSTALL_PYTHONDIR_DEFAULT "/usr/" ${Python_SITELIB})
	# "${CMAKE_INSTALL_LIBDIR}/python${PYTHON_VERSION_MAJOR}.${PYTHON_VERSION_MINOR}/site-packages"
endif()
set(CMAKE_INSTALL_PYTHONDIR "${CMAKE_INSTALL_PYTHONDIR_DEFAULT}"
  CACHE STRING "Location for installed python package"
  )


#------------------------------------------------------------
# Library version
#------------------------------------------------------------

write_basic_package_version_file(${PROJECT_NAME}ConfigVersion.cmake
  COMPATIBILITY SameMajorVersion
  VERSION ${PROJECT_VERSION}
  )
install(FILES ${CMAKE_CURRENT_BINARY_DIR}/${PROJECT_NAME}ConfigVersion.cmake
  DESTINATION ${CMAKE_INSTALL_LIBDIR}/cmake/${PROJECT_NAME})


#------------------------------------------------------------
# Install
#------------------------------------------------------------

install(
  FILES   ${CMAKE_CURRENT_SOURCE_DIR}/src/__init__.py
  DESTINATION ${CMAKE_INSTALL_PYTHONDIR}/${PROJECT_NAME}
  )
install(TARGETS cameopy
  EXPORT cameopyexport
  LIBRARY DESTINATION ${CMAKE_INSTALL_PYTHONDIR}/${PROJECT_NAME}
  )


#------------------------------------------------------------
# CPack
#------------------------------------------------------------

include(${CMAKE_CURRENT_SOURCE_DIR}/packaging/CPackConfig.cmake)
include(CPack)
