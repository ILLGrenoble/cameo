# Compile and Install

## Dependencies

Cameo depends on:
 - CMake (>=3.20)
 - Java JDK (>=11)
 - Maven
 - ZeroMQ
  
For the C++ API:
 - Rapidjson
 
For the Python API:
 - Pybind11

OpenJDK or Oracle JDK can be both installed.

### Debian-based Linux distribution
 
Install all the dependencies (except Java):

```
$ sudo apt install cmake maven libzmq3-dev libzmq-jni rapidjson-dev pybind11-dev doxygen
```

### Windows with vcpkg

To install the C++ and Python APIs, first install vcpkg, then install the dependencies:

```
$ vcpkg install zeromq
$ vcpkg install cppzmq

```


## Instructions

You can either generate the binaries or the Debian packages.

### Option 1: Generate the binaries

Compile:

```
$ cd cameo
$ cmake -S . -B build -D<OPTION>
$ cmake --build build
```

Possible options are:
 - CAMEO_ALL: to build everything including tests and examples
 - CAMEO_JAVA: to build all the Java artifacts
 - ZEROMQ_JAVA=*value*: value is 'jzmq' or 'jeromq'
 - CAMEO_PROXIES: to build the proxies
 - CAMEO_API_CPP=ON: to build and install the C++ API
 - CAMEO_API_PYTHON=ON: to build and install the Python API

If CAMEO_ALL is selected then all the options are selected.

For windows it is recommended to use the *jeromq* implementation.
With windows vcpkg, add the option -DCMAKE_TOOLCHAIN_FILE=path/to/vcpkg/vcpkg/scripts/buildsystems/vcpkg.cmake and compile the release objects:

```
$ cmake --build build --config Release
```

Install:

```
$ sudo cmake --build build --install --prefix <path/to/install>
```

With windows vcpkg, you can compile in full static with:

```
cmake -B build -S . -DCAMEO_ALL=ON -DZEROMQ_JAVA=jeromq -DBUILD_SHARED_LIBS=OFF -DVCPKG_TARGET_TRIPLET=x64-windows-static "-DCMAKE_MSVC_RUNTIME_LIBRARY=MultiThreaded$<$<CONFIG:Debug>:Debug>" "-DCMAKE_TOOLCHAIN_FILE=path/to/vcpkg/vcpkg/scripts/buildsystems/vcpkg.cmake"
```

You will need to install the static libraries of zeromq and cppzmq in vcpkg.


### Option 2: Generate the Debian packages

It is also possible to build and create Debian packages. In this case, please use the *build_and_package.sh* script.

```
$ ./build_and_package.sh <build directory>
```

The script provides the following *.deb* packages located in <build directory>/packages/.
They can be installed using package manager.

### Generate the documentation

Generate the C++ Doxygen:

```
$ cmake --build build --target doc
```
