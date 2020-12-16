#!/bin/bash

usage(){
    echo "`basename $0` [INSTALL_PREFIX] [CMAKE_PREFIX_PATH]" 
}


case $1 in
    dev|DEV|devel|DEVEL)
	DEV=true
	INSTALL_PREFIX=${2:-/tmp/devel}
	echo "[INFO] Development mode: installation directory is $INSTALL_PREFIX"
	ARGS="-DCMAKE_INSTALL_PREFIX=${INSTALL_PREFIX} -DCMAKE_PREFIX_PATH=$3"
	;;
    "")
	;;
    *)
	INSTALL_PREFIX=$1
	ARGS="-DCMAKE_INSTALL_PREFIX=${INSTALL_PREFIX} -DCMAKE_PREFIX_PATH=$2"
	;;
esac


# to force recompilation:
# cmake --build . --target cameo-api-cpp --clean-first
mkdir -p build/ 
cd build/
cmake .. $ARGS
# compile without the tests, because they need the rest to be compiled first
cmake --build . || {
    echo "[ERROR]" >> /dev/stderr
    exit 1
}
    
# if [ -n "$DEV" ];then
#     echo "Enabling testing"
#     # now enable the tests 
#     cmake  -DCAMEOPYTHON_TESTS=ON $ARGS ..
#     cmake --build .  || exit 1
# fi
