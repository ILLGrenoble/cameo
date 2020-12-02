# Compile and Install

## Dependencies
 - maven 

### Centos 8
``` yum install -y maven```
Update to the most recent version of JAVA
```sudo /sbin/alternatives --config java_sdk_openjdk```

## Instructions
Download the version:
```
git clone -b v1.1 --depth 1 https://code.ill.fr/cameo/cameo.git
```
Compile the Java sources:
```
cd cameo
mvn clean install
```
Install the jzmq version of the cameo-server:
```
install.sh 1.0.1
```


Compile and install the C++ API sources into a temporary directory e.g. */tmp/cameo-install* :
```
cd cameo-api-cpp
mkdir build
cd build
cmake -DCMAKE_INSTALL_PREFIX:PATH=/tmp/cameo-install ..
cmake --build . --target install
```
Get the include and so files from the temporary directory.
