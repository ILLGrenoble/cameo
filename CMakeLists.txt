cmake_minimum_required(VERSION 3.7.2)
# Project name and version
project(cameopy VERSION 1.0.0 LANGUAGES CXX)

include(GNUInstallDirs)

# Want to compile dynamic or static libraries
set(BUILD_SHARED_LIBS True)


include(ExternalProject)
#------------------------------------------------------------ Dependencies
# 2.4.3 is the version that has been used for development and tests
find_package(pybind11 2.4.3 QUIET)  
find_package(cameo 1.1 QUIET)
#------------------------------------------------------------


#------------------------------------------------------------
if(NOT pybind11_FOUND)
  message(WARNING "Using PYBIND11 from GIT repository")
  ExternalProject_Add(pybind11 
	GIT_REPOSITORY https://github.com/pybind/pybind11.git
	GIT_TAG v2.5.0
	GIT_SHALLOW True
	#INSTALL_COMMAND ""
	UPDATE_COMMAND ""
	UPDATE_DISCONNECTED FALSE
	CMAKE_ARGS -DPYBIND11_TEST=OFF -DPYBIND11_INSTALL=ON -DCMAKE_INSTALL_PREFIX=${CMAKE_CURRENT_BINARY_DIR}/pybind11_install/
	#I am obliged to perform the install step otherwise some files from pybind are not put together and I don't manage to set it.
	)
      ExternalProject_Get_Property(pybind11 install_dir BINARY_DIR)

      list(APPEND DEPENDENT_PACKAGES pybind11)
  #set(pybind11_DIR ${install_dir})
  set(pybind11_DIR ${CMAKE_CURRENT_BINARY_DIR}/pybind11_install/share/cmake/pybind11)
endif()

#------------------------------------------------------------
if(NOT cameo_FOUND)
  ExternalProject_Add(cameo
    GIT_REPOSITORY https://code.ill.fr/cameo/cameo-api-cpp.git
    #    GIT_TAG master_new
    GIT_TAG master
    GIT_REMOTE_NAME cameocpp
    GIT_SHALLOW True
    #SOURCE_DIR ${CMAKE_CURRENT_SOURCE_DIR}/cameo-api-cpp/
    INSTALL_DIR ${CMAKE_INSTALL_PREFIX} # this does not work...
    CMAKE_ARGS -DCMAKE_INSTALL_PREFIX=${CMAKE_INSTALL_PREFIX}
    #INSTALL_COMMAND "" # this disables the install step for the external project
    )
  ExternalProject_Get_Property(cameo install_dir BINARY_DIR)
  set(cameo_DIR ${BINARY_DIR})
  list(APPEND DEPENDENT_PACKAGES cameo)
  #set(cameo_DIR ${install_dir})
endif(NOT cameo_FOUND)


ExternalProject_Add(cameopython
  SOURCE_DIR ${CMAKE_CURRENT_SOURCE_DIR}/src/
  DEPENDS  ${DEPENDENT_PACKAGES}
  CMAKE_ARGS  -DCMAKE_INSTALL_PREFIX=${CMAKE_INSTALL_PREFIX} -DCMAKE_PREFIX_PATH=${CMAKE_PREFIX_PATH}  -Dpybind11_DIR=${pybind11_DIR} #pybind is fake-installed in the build
  #INSTALL_DIR ${CMAKE_INSTALL_PREFIX}
  )

enable_testing()

list(APPEND CMAKE_PREFIX_PATH ${CMAKE_INSTALL_PREFIX}/${CMAKE_INSTALL_LIBDIR})
ExternalProject_Add(testing
  SOURCE_DIR ${CMAKE_CURRENT_SOURCE_DIR}/test/
  DEPENDS ${DEPENDENT_PACKAGES}
  CMAKE_ARGS -DCMAKE_PREFIX_PATH=${CMAKE_PREFIX_PATH} 
  INSTALL_COMMAND ""
  )


#if(CAMEOPYTHON_TESTS)
#  message("TESTS have been enabled")
#  enable_testing()
#  
#  add_subdirectory(test)
#endif(CAMEOPYTHON_TESTS)
#unset(CAMEOPYTHON_TESTS CACHE)  # this removes the value from the CACHE, this is a trick to get the test compiled when running cmake --build . the second time

#ExternalProject_Add_StepDependencies(cameopython build cameocpp)


#find_package(cameo REQUIRED)
#message(${cameo_INCLUDE_DIRS})
#add_subdirectory(${CMAKE_CURRENT_BINARY_DIR}/${CPP_API})

# include directories
#include_directories(${PROJECT_SOURCE_DIR}/src)
#include_directories(${PYTHON_INCLUDE_DIRS})
#include_directories(${Boost_INCLUDE_DIRS})

# create the lib
#add_library(${MODULE_NAME} src/TimeCondition.cpp)
#target_link_libraries(${MODULE_NAME} ${Boost_LIBRARIES} ${${CPP_API}_INCLUDE_DIR})
#target_include_directories(${MODULE_NAME} ${cameo_INCLUDE_DIR})

# Copy the __init__.py file
#configure_file(__init__.py ${CMAKE_CURRENT_BINARY_DIR}/src/__init__.py COPYONLY)

# Suppress prefix "lib" because Python does not allow this prefix
#set_target_properties(${MODULE_NAME} PROPERTIES PREFIX "")

#install(TARGETS ${MODULE_NAME} __init__.py DESTINATION "${PYTHON_INSTALL_PATH}")
