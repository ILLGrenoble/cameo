# How to compile from the source directory:
```
cmake -S . -B build/
cmake --build build/
```

# Install from source
In order to install in non-standard directories:
```
cmake -S . -B build/ -DCMAKE_INSTALL_PREFIX=<your chosen basepath>
cmake --build build/
cmake --build build/ --target install
```


# How to create DEB packages from the build directory:
```
cpack -G DEB
```

Two packages are created:
 - -lib: the runtime library for the user
 - -dev: the development package with the public headers and cmake config files



# Running tests
To compile also the with the test programs:
```
cmake -S . -B build/ -DTESTS=ON ..
cmake --build build/
```
