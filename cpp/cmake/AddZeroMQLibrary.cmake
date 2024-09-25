macro(add_zeromq_library)

find_package(cppzmq QUIET)
if(NOT cppzmq_FOUND)
  message(STATUS "CMake cppzmq package not found!")

  # Use cppzmq CMake code
  if (NOT TARGET libzmq AND NOT TARGET libzmq-static)
  
    find_package(ZeroMQ QUIET)
	
    # libzmq autotools install: fallback to pkg-config
    if(NOT ZeroMQ_FOUND)
      message(STATUS "CMake libzmq package not found, trying again with pkg-config (normal install of zeromq)")
      # Two libzmq-pkg-config folders added for proxies and Python
      list (APPEND CMAKE_MODULE_PATH ${CMAKE_CURRENT_SOURCE_DIR}/../api/libzmq-pkg-config ${CMAKE_CURRENT_SOURCE_DIR}/../../cpp/api/libzmq-pkg-config)
      find_package(ZeroMQ REQUIRED)
    endif()
	
    # TODO "REQUIRED" above should already cause a fatal failure if not found, but this doesn't seem to work
    if(NOT ZeroMQ_FOUND)
      message(FATAL_ERROR "ZeroMQ was not found, neither as a CMake package nor via pkg-config")
    endif()
	
    if (ZeroMQ_FOUND AND NOT (TARGET libzmq OR TARGET libzmq-static))
      message(FATAL_ERROR "ZeroMQ version not supported!")
    endif()
  endif()
else()
  message(STATUS "CMake cppzmq package found")
endif()

endmacro()