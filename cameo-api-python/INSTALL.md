
First need to compile the cameo-api-cpp as a static library: follow the instructions 
in INSTALL.md in the cameo-api-cpp.

Then you need to tell CMake where the compiled cameo-api-cpp compiled is:
```
mkdir build/
cd build/
cmake .. -DCMAKE_PREFIX_PATH=../cameo-api-cpp/build/static/
cmake --build .
```
