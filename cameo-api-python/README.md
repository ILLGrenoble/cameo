# cameo-python

This library provides the Python API for Cameo.

 
## Compilation dependencies
   - cmake
The API is obtained using pybind11 and the C++ Cameo API.
   - [pybind11](https://github.com/pybind/pybind11)

Testing is performed using the doctest framework:
   - [doctest](https://github.com/onqtam/doctest)
   
These packages can either be installed on the system (see exact name of the packages below), or be downloaded from GitHub and compiled from source if not found on the system.

### CENTOS 8
```yum install -y cmake pybind11-devel```

For testing:
```yum install -y doctest-devel```

### Ubuntu 18
```sudo apt install python3-pybind11 libzmq3-dev```

For testing:
```sudo apt install doctest-dev```

## How to compile and install
```
cmake -S . -B <build_directory> -DCMAKE_PREFIX_PATH=<cpp_api_build_directory>
cmake --build <build_directory>
```

in order to install in a non-standard directory add the `-DCMAKE_INSTALL_PREFIX=<your_base_path>` to the first cmake invocation.

Don't forget to set your LD_LIBRARY_PATH to `/usr/local/lib64/`

Developers can follow the instructions [here](doc/DEVEL.md)
