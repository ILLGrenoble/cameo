
## Dependencies 
 - cmake 3.7.2
 - cppzmq
 - rapidjson
 - doxygen (optional)
 
### CENTOS 8 

List of packages:
 - cmake
 - cppzmq-devel
 - rapidjson-devel
 
```
pkgs="cmake zeromq-devel rapidjson-devel"
yum install -y $pkgs
```

### Debian XXX



### Ubuntu XXX
```sudo apt install cmake libzmq3-dev rapidjson-dev```


## Compilation instructions

```
mkdir build/
cd build/
cmake ..
cmake --build .
```

To compile also the with the test programs:
```
cmake -DTESTS=ON ..
cmake --build .
```


## Development 
How to find the list of public headers and check that they are all in the include/ directory
 1. move all the headers in src/
 2. move cameo.h to include/
 3. run the following command multiple times until there is no output
```
#!/bin/fish
set -l ORIG_DIR src
for f in (for file in include/*; grep include $file | grep '"'; end | sort | uniq | sed 's|.* "||;s|"||'); if [ -e $ORIG_DIR/$f ] ; echo $f; git mv $ORIG_DIR/$f include/; end; end
```
