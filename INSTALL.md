# Compile and Install

## Dependencies

CAMEO depends on:
- CMake (>=3.20)
- Java JDK (>=11)
- Maven
- ZeroMQ
  
For the C++ API:
 - Rapidjson
 
For the Python API:
 - Pybind11

OpenJDK or Oracle JDK can be both installed.

### Debian-based Linux
 
Install all the dependencies (except Java):

```
sudo apt install cmake maven libzmq3-dev libzmq-jni rapidjson-dev pybind11-dev doxygen
```

### Windows vcpkg

We recommend to use vcpkg to compile on Windows.
To install the C++ and Python APIs, first install vcpkg, then install the dependencies:

```
vcpkg install zeromq cppzmq rapidjson pybind11
```


## Instructions

You can either generate the binaries or the Debian packages.

### Option 1: Generate the binaries

Possible options are:
- CAMEO_ALL=ON: to build everything including tests and examples
- CAMEO_JAVA=ON: to build all the Java artifacts
- ZEROMQ_JAVA=*value*: value is 'jzmq' or 'jeromq'
- CAMEO_PROXIES=ON: to build the proxies
- CAMEO_API_CPP=ON: to build and install the C++ API
- CAMEO_API_PYTHON=ON: to build and install the Python API
- CAMEO_DOC=ON: to build the documentation

If CAMEO_ALL is selected then all the options are selected except CAMEO_DOC.


#### Debian-based Linux

Configure and compile:

```
cd cameo
cmake -S . -B build -D<OPTION>
cmake --build build
```


#### Windows vcpkg

The options are still valid and the configuration is done with:

```
cd cameo
cmake -S . -B build -D<OPTION>
```

It is recommended on Windows to use the *jeromq* implementation since *jzmq* is not well compiled.
To use vcpkg, add the option -DCMAKE_TOOLCHAIN_FILE=path/to/vcpkg/vcpkg/scripts/buildsystems/vcpkg.cmake and compile the release objects:

```
cmake --build build --config Release
```

The *dll* library file is generated.
You can also compile in full static. Install the static dependencies:

```
vcpkg install zeromq:x64-windows-static cppzmq:x64-windows-static
```

Configure the build with:

```
cmake -B build -S . -DCAMEO_ALL=ON -DZEROMQ_JAVA=jeromq -DBUILD_SHARED_LIBS=OFF -DVCPKG_TARGET_TRIPLET=x64-windows-static "-DCMAKE_MSVC_RUNTIME_LIBRARY=MultiThreaded$<$<CONFIG:Debug>:Debug>" "-DCMAKE_TOOLCHAIN_FILE=path/to/vcpkg/vcpkg/scripts/buildsystems/vcpkg.cmake"
```

The static library file will be generated.
You can compile the library:

```
cmake --build build --config Release
```

And install it. The default location can be overriden using *--prefix* option:

```
cmake --install build --config Release --prefix "path\to\install"
```


### Option 2: Generate the Debian packages

It is also possible to build and create Debian packages. In this case, please use the *build_and_package.sh* script.

```
./build_and_package.sh <build directory>
```

The script provides the following *.deb* packages located in <build directory>/packages/.
They can be installed using package manager.

### Generate the documentation

Generate the C++ Doxygen:

```
cmake --build build --target doc
```
