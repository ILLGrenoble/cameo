if(NOT DEFINED PROJECT_NAME)
  cmake_minimum_required(VERSION 3.7.2)

  # Project name and version
  project(cameopy VERSION 1.0.0 LANGUAGES CXX)
endif()

find_package(Git REQUIRED)

#------------------------------
find_package(doctest QUIET)
if(NOT doctest_FOUND)
  ExternalProject_Add(
    doctest
    PREFIX ${CMAKE_BINARY_DIR}/doctest
    GIT_REPOSITORY https://github.com/onqtam/doctest.git
    TIMEOUT 10
    UPDATE_COMMAND ${GIT_EXECUTABLE} pull
    CONFIGURE_COMMAND ""
    BUILD_COMMAND ""
    INSTALL_COMMAND ""
    LOG_DOWNLOAD ON
    )
  # Expose required variable (DOCTEST_INCLUDE_DIR) to parent scope
  ExternalProject_Get_Property(doctest source_dir)
  set(DOCTEST_INCLUDE_DIR ${source_dir}/ CACHE INTERNAL "Path to include folder for doctest")
endif()

#------------------------------
find_package(cameo 1.0 REQUIRED)

#------------------------------
add_executable(testA.out testA.cpp)
target_link_libraries(testA.out # this gets also the public include dir from the BUILD_INTERFACE
  PRIVATE cameo
  )

#file(GENERATE OUTPUT /tmp/message.log CONTENT $<TARGET_PROPERTY:cameo,INTERFACE_INCLUDE_DIRECTORIES>)

if(doctest_FOUND)
  target_link_libraries(testA.out PRIVATE doctest::doctest)
else()
  target_include_directories(testA.out
    PRIVATE $<BUILD_INTERFACE:${DOCTEST_INCLUDE_DIR}>
    )
endif()

enable_testing()
# testA.out has been added to the cameo_config.xml file as testA
add_test(self          cmo -e tcp://localhost:2000 exec testA -s --test-case=self)
add_test(instance      cmo -e tcp://localhost:2000 exec testA -s --test-case=instance)
add_test(requesterCpp  cmo -e tcp://localhost:2000 exec testA -s --test-case=requester --subcase-exclude=python)
add_test(requesterPy   cmo -e tcp://localhost:2000 exec testA -s --test-case=requester --subcase=python)
add_test(publisherPy   cmo -e tcp://localhost:2000 exec testA -s --test-case=subscriber --subcase=python)
add_test(subscriberCpp  cmo -e tcp://localhost:2000 exec testA -s --test-case=subscriber --subcase-exclude=python)
add_test(all  cmo -e tcp://localhost:2000 exec testA -s --test-case-exclude=self)



# add_executable(test1 test1.cpp)
# target_include_directories(test1
#   PRIVATE $<BUILD_INTERFACE:${CMAKE_BINARY_DIR}/cameocpp-prefix/src/cameocpp/src/cameo>
#   PRIVATE $<BUILD_INTERFACE:${CMAKE_SOURCE_DIR}/doctest/doctest/>
#   )
# target_include_directories(test1 PRIVATE ${CMAKE_SOURCE_DIR}/src/)
# target_link_libraries(test1 PRIVATE cameo)
# add_test(test1 test1)

# add_executable(testB.out testB.cpp)
# target_include_directories(testB.out
#   PRIVATE $<BUILD_INTERFACE:${CMAKE_SOURCE_DIR}/include> $<BUILD_INTERFACE:${CMAKE_SOURCE_DIR}/doctest/doctest/>
#   )
# target_include_directories(testB.out PRIVATE ${CMAKE_SOURCE_DIR}/src/)
# target_link_libraries(testB.out PRIVATE cameo)

