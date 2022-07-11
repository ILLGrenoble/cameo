set(CPACK_WARN_ON_ABSOLUTE_INSTALL_DESTINATION ON)
set(CPACK_PACKAGE_CHECKSUM MD5)

set(CPACK_PACKAGE_CONTACT "Yannick Le Goc <legoc@ill.fr>")
set(CPACK_PACKAGE_VENDOR "ILL")
set(CPACK_PACKAGE_DESCRIPTION_SUMMARY "Cameo C++ API")

set(CPACK_GENERATOR "DEB")
# #set(CPACK_PACKAGE_DESCRIPTION_FILE "/home/andy/vtk/CMake/Copyright.txt")
set(CPACK_PACKAGE_DESCRIPTION "Long description")
set(CPACK_RESOURCE_FILE_README ${CMAKE_CURRENT_SOURCE_DIR}/README.md)


set(CPACK_DEBIAN_FILE_NAME DEB-DEFAULT)
set(CPACK_RPM_FILE_NAME RPM-DEFAULT)

set(CPACK_RPM_BUILDREQUIRES "cmake >= ${CMAKE_MINIMUM_REQUIRED_VERSION}")

set(CPACK_DEBIAN_PACKAGE_SECTION libs)

# set(CPACK_RPM_PACKAGE_RELOCATABLE ON)
# set(CPACK_RPM_PACKAGE_LICENSE "EUPL v1.1")

set(CPACK_COMPONENTS_IGNORE_GROUPS ON)
set(CPACK_DEB_COMPONENT_INSTALL TRUE)

set(CPACK_RPM_COMPONENT_INSTALL TRUE)
set(CPACK_DEBIAN_ENABLE_COMPONENT_DEPENDS ON)



set(CPACK_COMPONENT_${component_runtime}_REQUIRED ON)
set(CPACK_COMPONENT_${component_development}_REQUIRED OFF)
set(CPACK_COMPONENT_${component_development}_DEPENDS ${component_runtime} )
set(CPACK_COMPONENT_${component_runtime}_DEPENDS "" )

set(CPACK_COMPONENT_${component_runtime}_DESCRIPTION "Shared library of the C++ API")
set(CPACK_COMPONENT_${component_development}_DESCRIPTION "Headers and development files for the C++ API")

set(CPACK_DEBIAN_PACKAGE_SHLIBDEPS ON)
set(CPACK_DEBIAN_PACKAGE_GENERATE_SHLIBS ON )
set(CPACK_DEBIAN_PACKAGE_GENERATE_SHLIBS_POLICY ">=")

set(CPACK_DEBIAN_${component_runtime}_PACKAGE_DEPENDS "libzmq5")
set(CPACK_DEBIAN_${component_development}_PACKAGE_DEPENDS "")
set(CPACK_RPM_${component_runtime}_PACKAGE_REQUIRES "cameo-server >= 2.0.0, zeromq")
set(CPACK_RPM_${component_development}_PACKAGE_DEPENDS "")

set(CPACK_DEBIAN_${component_runtime}_PACKAGE_NAME ${LIBNAME}-lib)
set(CPACK_DEBIAN_${component_development}_PACKAGE_NAME ${LIBNAME}-dev)
set(CPACK_RPM_${component_runtime}_PACKAGE_NAME ${LIBNAME}-lib)
set(CPACK_RPM_${component_development}_PACKAGE_NAME ${LIBNAME}-devel)


set(CPACK_DEBIAN_${component_development}_PACKAGE_SECTION "libdevel")
# #if((${CMAKE_BUILD_TYPE} EQUAL "Debug") OR (${CMAKE_BUILD_TYPE} EQUAL "RelWithDebInfo"))
# #  set(CPACK_DEBIAN_DEBUGINFO_PACKAGE ON)
# #  set(CPACK_RPM_DEBUGINFO_PACKAGE ON)
# #endif()

# ####### CPACK RPM
# #message("CPACK SYSTEM NAME: ${CMAKE_SYSTEM_NAME}")



# cpack_add_component(lib
#   DISPLAY_NAME "LIBB"
#   DESCRIPTION "runtime library"
#   REQUIRED #[HIDDEN | REQUIRED | DISABLED ]
#   GROUP LIBBB
#   #DEPENDS 
#   #[INSTALL_TYPES type1 type2 ... ]
#   #[DOWNLOADED]
#   #[ARCHIVE_FILE filename]
#   #[PLIST filename]
#   )

# cpack_add_component(dev
#   DISPLAY_NAME "DEVV"
#   DESCRIPTION "library development"
#   #REQUIRED #[HIDDEN | REQUIRED | DISABLED ]
#   GROUP DEVVV
#   DEPENDS lib
#   #[INSTALL_TYPES type1 type2 ... ]
#   #[DOWNLOADED]
#   #[ARCHIVE_FILE filename]
#   #[PLIST filename]
#   )

