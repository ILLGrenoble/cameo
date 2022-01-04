## Dependencies for testing:
Ubuntu bionic: `sudo apt install doctest-dev`


## How to compile and install
```
cmake -S . -B <build_directory> -DCMAKE_PREFIX_PATH=<cpp_api_build_directory>
cmake --build <build_directory>
```

in order to install in a non-standard directory add the `-DCMAKE_INSTALL_PREFIX=<your_base_path>` to the first cmake invocation.


If you are using a non-standard path, the following environmental variables should then be set.
```
#!/bin/bash
export CMAKE_INSTALL_PREFIX=MY_INSTALL_PATH
export PYTHONPATH=${PYTHONPATH}:${CMAKE_INSTALL_PREFIX}/lib64/python3.6/site-packages/
export LD_LIBRARY_PATH=${LD_LIBRARY_PATH} ${CMAKE_INSTALL_PREFIX}/lib64
```
Example for the fish shell:
```
#!/bin/fish
set -lx CMAKE_INSTALL_PREFIX /tmp/devel/
set -lx PYTHONPATH $PYTHONPATH $CMAKE_INSTALL_PREFIX/lib64/python3.6/site-packages/
set -lx LD_LIBRARY_PATH $CMAKE_INSTALL_PREFIX/lib64/
```

# Testing
For testing you need CAMEO to be installed in order to run the cameo_server

In order to run the tests, just type
```./test.sh```
from the source directory root.

### CENTOS
The java.library.path is the LD_LIBRARY_PATH + [/usr/java/packages/lib, /usr/lib64, /lib64, /lib, /usr/lib]
Point the LD_LIBRARY_PATH to the place where the libjzmq.so is stored

```
fish
set -x LD_LIBRARY_PATH /usr/share/java/ /usr/local/lib/
/usr/lib/jvm/java-14-openjdk-14.0.1.7-2.rolling.el8.x86_64/bin/java -jar "/usr/local/share/java/cameo-server.jar" test/cameo_config.xml

# list the test cases:
cmo -e tcp://localhost:2000 exec testA --list-test-cases --verbose

cmo -e tcp://localhost:2000 exec testA -s --test-case=self
cmo -e tcp://localhost:2000 exec testA -s --test-case=instance
cmo -e tcp://localhost:2000 exec testA -s --test-case=requester --subcase-exclude=python

ctest --output-on-failure
```

# Testing Ubuntu
```
bash
export LD_LIBRARY_PATH=/usr/share/java/:/usr/lib/x86_64-linux-gnu/jni
cameo-server test/cameo_config.xml
```



cmake .. -DopenPMD_USE_PYTHON=OFF -DopenPMD_INSTALL=OFF -DopenPMD_USE_INTERNAL_CATCH=ON -DBUILD_TESTING=OFF -DBUILD_EXAMPLES=OFF -DBUILD_CLI_TOOLS=ON





 cmake .. -DCMAKE_INSTALL_PREFIX=/tmp/devel3 -Dcameo_DIR=/tmp/devel2/lib64/cmake/
