# Compile and Install

## Dependencies

Cameo depends on:
 - CMake
 - Java (>=9)
 - Maven 
 
### Centos 8

Install the components:

```
$ yum install -y maven java-latest-openjdk-devel
```

Update to the most recent version of Java:

```
$ sudo /sbin/alternatives --config java_sdk_openjdk
```

### Ubuntu

Install Maven:

```
$ sudo apt install maven
```

## Instructions

Download the latest version:

```
$ git clone --depth 1 https://code.ill.fr/cameo/cameo.git
```

### Option 1: Generate the binaries

Compile:

```
$ cd cameo/
$ cmake -S . -B build/ -D<OPTION>
$ cmake --build build/
```

Possible options are:
 - CMAKE_INSTALL_PREFIX=<your_chosen_install_basepath>: to install in a non-standard directory
 - CAMEO_API_CPP=ON: to build and install the C++ API
 - CAMEO_API_PYTHON=ON: to build and install the Python API
 - CAMEO_TESTS=ON: to build the Java and C++ tests
 
Install:

```
$ sudo cmake --build . --target install
```

### Option 2: Generate the Debian packages

It is also possible to build and create Debian packages. In this case, please use the build_and_package.sh script.

```
$ ./build_and_package.sh <build_directory>
```

The script provides the following .deb packages located in <build_directory>/packages/
They can be installed using package manager.

