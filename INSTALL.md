# Compile and Install

## Dependencies
 - maven 
 - java (>=9)
 
### Centos 8
``` yum install -y maven java-latest-openjdk-devel ```

Update to the most recent version of JAVA
``` sudo /sbin/alternatives --config java_sdk_openjdk ```

### Ubuntu
``` sudo apt install maven```
## Instructions
Download the version:
```
git clone -b v1.1 --depth 1 https://code.ill.fr/cameo/cameo.git
```

### Option1: maven+cmake
Compile:
```
cd cameo/
cmake -S . -B build/ -D<OPTION>
cmake --build build/
```
Possible options are:
 - CMAKE_INSTALL_PREFIX=<your_chosen_install_basepath>: to install in a non-standard directory
 - CAMEO_API_CPP=ON: to build and install the C++ API
 - CAMEO_API_PYTHON=ON: to build and install the Python API
 
Install:
```
sudo cmake --build . --target install
```
### Option2: cmake + cpack = DEB package
It is also possible to build and create Debian packages. In this case, please use the build_and_package.sh script.
```
./build_and_package.sh <build_directory>
```
The script provides the following .deb packages located in <build_directory>/packages/
They can be installed using package manager.

