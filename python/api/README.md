# cameo-api-python

This library provides the Python API for Cameo.
 
## Compilation dependencies

Dependencies:
- CMake 3.12.0
- [Pybind11](https://github.com/pybind/pybind11): The API is obtained using pybind11 and the C++ Cameo API. 
If the required version is not available on the system, it is automatically downloaded from source and compiled in place.
- [Doctest](https://github.com/onqtam/doctest)
Testing is performed using the doctest framework.
- Cameo API library: The static library needs to be compiled first. See [here](../cameo-api-cpp/README.md) for compilation instructions of cameo-cpp-api.

For convenience, the packages for some major distributions are listed in the following:
- CENTOS 8: `yum install -y cmake pybind11-devel`
- Ubuntu: `sudo apt install libzmq3-dev python3-dev python3-distutils`

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
They will be found in `<build directory>/packaging`.

Don't forget to set your LD_LIBRARY_PATH to `/usr/local/lib64/`
Developers can follow the instructions [here](doc/DEVEL.md).
