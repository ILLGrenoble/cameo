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

### Debian-Based Linux distribution
 
Install all the dependencies (except Java):

```
sudo apt install cmake maven libzmq3-dev libzmq-jni rapidjson-dev pybind11-dev doxygen
```
 
### Centos 8

To be filled.



## Instructions

You can either generate the binaries or the Debian packages.

### Option 1: Generate the binaries

Compile:

```
$ cd cameo/
$ cmake -S . -B build/ -D<OPTION>
$ cmake --build build/
```

Possible options are:
 - ZEROMQ_JAVA=*value*: value is 'jzmq' or 'jeromq'
 - CMAKE_INSTALL_PREFIX=*directory*: to install in a non-standard directory
 - CAMEO_API_CPP=ON: to build and install the C++ API
 - CAMEO_API_PYTHON=ON: to build and install the Python API
 - CAMEO_TESTS=ON: to build the Java and C++ tests
 - CAMEO_EXAMPLES=ON: to build the Java and C++ examples
 
Install:

```
$ sudo cmake --build . --target install
```

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
$ cmake --build . --target doc
```
