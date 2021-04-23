# cameo-api-python

This library provides the Python API for Cameo.

 
## Compilation dependencies
- cmake 3.12.0
- [pybind11](https://github.com/pybind/pybind11)
The API is obtained using pybind11 and the C++ Cameo API. If the required version is not available on the system, it is automatically downloaded from source and compiled in place.
- [doctest](https://github.com/onqtam/doctest)
Testing is performed using the doctest framework.
- cameo-api-cpp-static: the static library need to be compiled first. See [here](../cameo-api-cpp/REDME.md) for compilation instructions of cameo-cpp-api.

For convenience, the packages for some major distributions are listed in the following:

 - CENTOS 8: `yum install -y cmake pybind11-devel`
 - Ubuntu bionic: `sudo apt install libzmq3-dev`
 *N.B.* `pybind11-dev` has version 2.0.1 while 2.4.3 is needed -> most recent version automatically downloaded and compiled from source

## How to compile and install

```
cmake -S . -B <build directory> -DCMAKE_PREFIX_PATH="<cpp api build directory> -D<OPTION>"
cmake --build <build directory>
```
Possible options are:
 - CMAKE_INSTALL_PREFIX=<your_chosen_install_basepath>: to install in a non-standard directory

Debian packages can also be created by:
```
cpack --config <build directory>/CPackConfig.cmake -B <build directory>/packaging 
```
and be found in `<build directory>/packaging`


Don't forget to set your LD_LIBRARY_PATH to `/usr/local/lib64/`

Developers can follow the instructions [here](doc/DEVEL.md)
