cmake_minimum_required(VERSION 3.20.0)

# Project name and version
project(cameopy
  VERSION 2.1.2
  LANGUAGES CXX
  HOMEPAGE_URL "https://code.ill.fr/cameo/cameo"
  )

#--------------- Helpers
include(GNUInstallDirs)
include(CMakePackageConfigHelpers)
include(FetchContent)


# in order to find FindZeroMQ.cmake
list(APPEND CMAKE_MODULE_PATH ${CMAKE_CURRENT_SOURCE_DIR}/../../cmake/)

#------------------------------------------------------------
# Dependencies
#------------------------------------------------------------
#list(APPEND CMAKE_PREFIX_PATH ${CMAKE_CURRENT_SOURCE_DIR}/../cameo-api-cpp/build/)
#message(STATUS ${CMAKE_MODULE_PATH})
find_package(cameo-api-cpp-static 2.1 REQUIRED) # provided by the C++ API
find_package(Python COMPONENTS Interpreter Development)
find_package(pybind11 2.9 CONFIG)

if(NOT pybind11_FOUND)
  message(WARNING "Using pybind11 from GIT repository")
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

#file(GENERATE OUTPUT /tmp/message.log CONTENT $<TARGET_PROPERTY:cameo,INTERFACE_INCLUDE_DIRECTORIES>)
target_link_libraries(cameopy PRIVATE Cameo::cameo-api-cpp-static)
#target_compile_features(cameopy PUBLIC cxx_std_17)
#target_include_directories(cameopy
#  PRIVATE $<TARGET_PROPERTY:Cameo::cameo-api-cpp,INTERFACE_INCLUDE_DIRECTORIES>
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
  file(RELATIVE_PATH CMAKE_INSTALL_PYTHONDIR_DEFAULT "/usr/" ${Python_SITELIB})
	# "${CMAKE_INSTALL_LIBDIR}/python${PYTHON_VERSION_MAJOR}.${PYTHON_VERSION_MINOR}/site-packages"
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
