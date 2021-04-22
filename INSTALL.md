# Compile and Install

## Dependencies
 - maven 

### Centos 8
``` yum install -y maven java-latest-openjdk-devel ```
Update to the most recent version of JAVA

``` sudo /sbin/alternatives --config java_sdk_openjdk ```

## Instructions
Download the version:
```
git clone -b v1.1 --depth 1 https://code.ill.fr/cameo/cameo.git
```


### Option1: 
Compile the Java sources:
```
cd cameo
mvn clean install
```
Install the jzmq version of the cameo-server:
```
install.sh 1.0.1
```

### Option2:
```
cmake -S . -B build/
cmake --build build/
```

Packages can be installed directly:
```
cmake --build . --target install
```
or they can be first packaged in either DEB or RPM format
```
cpack -G RPM|DEB
```
and then installed with your package manager



Compile and install the C++ API sources into a temporary directory e.g. */tmp/cameo-install* :
```
cd cameo-api-cpp
mkdir build
cd build
cmake -DCMAKE_INSTALL_PREFIX:PATH=/tmp/cameo-install ..
cmake --build . --target install
```
Get the include and so files from the temporary directory.




```
rm build/ /tmp/cpp/ /tmp/python/ -R
cmake -S . -B build/
cmake --build build/

cmake -S cameo-api-cpp -B build/cpp/
cmake -S cameo-api-python -B build/python/ -DCMAKE_PREFIX_PATH=build/cpp
cmake --build build/cpp/
cmake --build build/python/

cpack --config build/CPackConfig.cmake -B build/packaging/
cpack --config
```
