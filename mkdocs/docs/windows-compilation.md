For CAMEO 2.0, the compilation for Windows is reviewed. Here is the current status.
The compilations have been made on *sci-windows* with the *legoc* account.

Prerequisites:
* Visual Studio 19
* Cmake 3.19

### ZeroMQ 4.3.3

1. Create the directory *build* in *zeromq-4.3.3*
2. Launch cmake-gui
    * Source code: *C:/Users/legoc/Documents/zmq/zeromq-4.3.3*
    * Build: *C:/Users/legoc/Documents/zmq/zeromq-4.3.3/build*
    * Configure, Generate, Open Project (launches Visual Studio 19)  
  
3. In Visual Studio 19
    * Switch to Release Configuration
    * Generate Solution
      * The generated *dll* file is in *build/bin/Release*
      * The generated *lib* file is in *build/lib/Release*

### JZMQ

It must be built with the version of ZeroMQ. Follow the instructions at https://github.com/zeromq/jzmq.  
HTTP_PROXY and HTTPS_PROXY must be set to clone the project.

```
cmake -DCMAKE_VERBOSE_MAKEFILE:BOOL=ON .. -G "NMake Makefiles" -DZMQ_C_INCLUDE_PATH=C:\Users\legoc\Documents\zmq\zeromq-4.3.3\include -DZMQ_C_LIB_PATH=C:\Users\legoc\Documents\zmq\zeromq-4.3.3\lib
```
  Curious: Found JNI: in jdk 1.8 and not 10 like Java
```
nmake
```
Terminates with the error when generating files from native methods: could not find class file for org.zeromq.ZMQ

By trying to compile on another computer, it was another error at the previous stage (building zmq.jar).

To do:

* Remove the jdk 1.8 and retry the process.
* Try to fix the errors that must come from the configuration files that must be updated.

We have two options:

- Do not use JZMQ on Windows and use JeroMQ instead, hoping that it is stable enough for our puposes. 
- Still use the old ZeroMQ 4.0.5, but that means compiling *cameo-api-cpp* with ZeroMQ 4.0.5.

### RapidJSON
  Version 1.1.0 in *C:/Users/legoc/Documents/zmq/rapidjson-1.1.0*

### CAMEO API C++

1. Create the directory *build* in *cameo-api-cpp*
2. Create *C:/Users/legoc/Documents/zmq/zeromq-4.3.3/lib*
    * Copy *C:/Users/legoc/Documents/zmq/zeromq-4.3.3/build/lib/Debug/libzmq-v142-mt-gd-4_3_3.lib* to it
    * Copy *C:/Users/legoc/Documents/zmq/zeromq-4.3.3/build/lib/Release/libzmq-v142-mt-4_3_3.lib* to it
    * Indeed the script FindZeroMQ.cmake is searching for *ZMQ_ROOT/lib/\*.lib*

2. Launch cmake-gui
    * Source code: *C:/Users/legoc/workspace/cameo/cameo-api-cpp*
    * Build: *C:/Users/legoc/workspace/cameo/cameo-api-cpp/build*
    * Options:
      * _ZeroMQ_ROOT C:\Users\legoc\Documents\zmq\zeromq-4.3.3
      * ZeroMQ_LIBRARY_RELEASE C:\Users\legoc\Documents\zmq\zeromq-4.3.3\lib\libzmq-v142-mt-4_3_3.lib
      * ZeroMQ_LIBRARY_DEBUG C:\Users\legoc\Documents\zmq\zeromq-4.3.3\lib\libzmq-v142-mt-gd-4_3_3.lib

    * Configure, Generate, Open Project (launches Visual Studio 19)
      * Generate cameo-api-cpp-static
      * Files generated:
        *  C:\Users\legoc\workspace\cameo\cpp\api\build\cameo-api-cpp-obj.dir\Debug\cameo-api-cpp-obj.lib
        * C:\Users\legoc\workspace\cameo\cpp\api\build\cameo-api-cpp-obj.dir\Release\cameo-api-cpp-obj.lib

Problems
* The rapidjson include directory is not added to the include paths
* The zmq include directory is not added to the include paths
* The file *zmq.hpp* does not exist in *zeromq-4.3.3* (it is in the Linux package): we copy it from Linux in *zmq/* and add *zmq/* to the include paths

Corrections
* Added the include paths to cameo-api-cpp-obj: C:\Users\legoc\Documents\zmq;C:\Users\legoc\Documents\zmq\zeromq-4.3.3\include

Notes
* It would be better to add an optional property to specify where is the *zmq.hpp* file that could be downloaded or cloned
* It should be possible to use **nmake** to compile in command-line

### CAMEO API C++ Tests

```
set PATH=C:\Users\legoc\Documents\zmq\bin\4.3.3;%PATH%
```

The tests were not run yet.

### Protocol Buffers

The library is no longer required to compile CAMEO API C++ but it is used in the CAMEO Dielectrics program.  
1. Create the directory *C:/Users/legoc/Documents/zmq/protobuf-3.12.4/cmake/build*
2. Launch cmake-gui

* Source code: *C:/Users/legoc/Documents/zmq/protobuf-3.12.4/cmake*
* Build: *C:/Users/legoc/Documents/zmq/protobuf-3.12.4/cmake/build*
* Configure, Generate, Open Project (launches Visual Studio 19)

Notes:

* The documentation suggests to use *nmake* however using it compiles the library in *x86* format. Maybe an option is missing.
* It was necessary to switch the flag *Runtime Library* from */MT* to */MD* to conform to the other projects. Then only the libprotobuf.lib is linking.