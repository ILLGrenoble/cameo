#!/bin/bash

BASE_BUILD_DIR=${1:-/dev/shm/cameo/}
packages_dir=${BASE_BUILD_DIR}/packages
mkdir ${packages_dir} -p
#mvn  install

function mvPack(){
	mv ${build_dir}/packaging/*.deb ${packages_dir}/
}

#--------------- JAVA
for source_dir in cameo-server-jzmq cameo-console-jzmq
do
	build_dir=$BASE_BUILD_DIR/$source_dir
	cmake -S $source_dir -B $build_dir/ || exit 1
	cmake --build $build_dir || exit 1
	cpack --config $build_dir/CPackConfig.cmake -B $build_dir/packaging/ || exit 2
	mvPack
done

#--------------- API
#---------- C++
cmake -S cameo-api-cpp -B $BASE_BUILD_DIR/cameo-api-cpp/
cmake --build $BASE_BUILD_DIR/cameo-api-cpp/
cpack --config $BASE_BUILD_DIR/cameo-api-cpp/CPackConfig.cmake -B $BASE_BUILD_DIR/cameo-api-cpp/packaging 
mvPack

#---------- Python
build_dir=$BASE_BUILD_DIR/cameo-api-python/
cmake -S cameo-api-python -B $build_dir -DCMAKE_PREFIX_PATH=$BASE_BUILD_DIR/cameo-api-cpp/ || exit 1
cmake --build $build_dir || exit 1
cpack --config $build_dir/CPackConfig.cmake -B $build_dir/packaging/ || exit 1
mvPack
