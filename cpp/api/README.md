# cameo-api-cpp

This library provides the C++ API for Cameo.


## Compilation dependencies 
 - cmake 3.12.0
 - cppzmq
 - [rapidjson](https://github.com/Tencent/rapidjson)
 If not found on the system it is automatically downloaded and installed
 - doxygen (optional)
 
For convenience, the packages for some major distributions are listed in the following:

 - CENTOS 8: `yum install -y cmake cppzmq-devel rapidjson-devel`
 - Debian 10:
 - Ubuntu : `sudo apt install cmake libzmq3-dev rapidjson-dev`


## How to compile and install
```
cmake -S . -B <build directory> -D<OPTION>
cmake --build <build directory>
```
Possible options are:
 - CMAKE_INSTALL_PREFIX=<your_chosen_install_basepath>: to install in a non-standard directory

In order to install from source:
```
cmake --build <build directory> --target install
```

Debian packages can also be created by:
```
cpack -G DEB --config <build directory>/CPackConfig.cmake -B <build directory>/packaging 
```
and be found in `<build directory>/packaging`

Two packages are created:
 - -lib: the runtime library for the user
 - -dev: the development package with the public headers and cmake config files



## Running tests
To compile also the with the test programs:
```
cmake -S . -B build/ -DCAMEO_API_CPP_TESTS=ON ..
cmake --build build/
```

Developers can follow the instructions [here](doc/DEVEL.md)