if(NOT DEFINED PROJECT_NAME)
  cmake_minimum_required(VERSION 3.7.2)
  
  # Project name and version
  project(cameo VERSION 0.3.0 LANGUAGES CXX)
endif()
include(GNUInstallDirs)
include(CMakePackageConfigHelpers)
if(NOT DEFINED BUILD_SHARED_LIBS)
  set(BUILD_SHARED_LIBS True)
endif()

# fix the RPATH for the linker
#if(not APPLE)
#  set(CMAKE_INSTALL_RPATH $ORIGIN)
#endif()

#------------------------------------------------------------
# Dependencies
#------------------------------------------------------------
list(APPEND CMAKE_MODULE_PATH ${CMAKE_CURRENT_SOURCE_DIR})
find_package(ZeroMQ REQUIRED) # I cannot find it
find_package(Protobuf 3.0.0 REQUIRED)


#------------------------------------------------------------
# Libraries
#------------------------------------------------------------


#---------------
# Files
file(GLOB_RECURSE CAMEO_SOURCE_FILES "./cameo/*.cpp")
file(GLOB CAMEO_PUBLIC_HEADER_FILES "./cameo/*.h")
#message(${CAMEO_PUBLIC_HEADER_FILES})
#message(${CAMEO_SOURCE_FILES})

# this generates the Messages.pb.h and Messages.pb.cc files
protobuf_generate_cpp(PROTO_SRCS PROTO_HDRS  ./proto/Messages.proto)

add_library(cameo ${CAMEO_SOURCE_FILES} ${PROTO_SRCS})
target_link_libraries(cameo PRIVATE zmq protobuf ${Protobuf_LIBRARIES})
target_include_directories(cameo
  PRIVATE ${CMAKE_CURRENT_BINARY_DIR} # this is to get protobuf messages
  PRIVATE ${CMAKE_CURRENT_SOURCE_DIR}/src/cameo/impl
  PUBLIC $<BUILD_INTERFACE:${CMAKE_CURRENT_SOURCE_DIR}/cameo>
  PUBLIC $<INSTALL_INTERFACE:${CMAKE_INSTALL_INCLUDEDIR}>
  )
#target_compile_features(cameo PUBLIC cxx_std_11)
set_target_properties(cameo PROPERTIES
  SOVERSION ${PROJECT_VERSION_MAJOR}
  VERSION ${PROJECT_VERSION}
  )

message(${PROJECT_VERSION_MAJOR})
message(${PROJECT_VERSION})
#---------------
# Library version
write_basic_package_version_file(cameoConfigVersion.cmake
  COMPATIBILITY SameMajorVersion
  VERSION ${PROJECT_VERSION}
  )


#------------------------------------------------------------
# Install
#------------------------------------------------------------
install(FILES ${CMAKE_CURRENT_BINARY_DIR}/cameoConfigVersion.cmake
  DESTINATION ${CMAKE_INSTALL_LIBDIR}/cmake)

# this is to install the headers
install(FILES ${CAMEO_PUBLIC_HEADER_FILES}
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
