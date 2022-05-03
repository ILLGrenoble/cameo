#!/bin/bash

BASE_BUILD_DIR=${1:-/dev/shm/cameo/}
[ "$BASE_BUILD_DIR" != "${BASE_BUILD_DIR#/}" ] || BASE_BUILD_DIR=$PWD/$BASE_BUILD_DIR

packages_dir=${BASE_BUILD_DIR}/packages
mkdir ${packages_dir} -p
#mvn  install

function mvPack(){
	mv ${build_dir}/packaging/*.deb ${packages_dir}/
}

#--------------- JAVA
for source_dir in java/server-jzmq java/console-jzmq
do
	echo "COMPONENT: $source_dir"
	build_dir=$BASE_BUILD_DIR/$source_dir
	cmake -S $source_dir -B $build_dir/ || exit 1
	cmake --build $build_dir || exit 1
	cpack -V --config $build_dir/CPackConfig.cmake -B $build_dir/packaging/ || exit 2
	mvPack
done

#--------------- API
#---------- C++
source_dir=cpp/api
build_dir=$BASE_BUILD_DIR/$source_dir
cmake -S $source_dir -B $build_dir -DCMAKE_INSTALL_PREFIX=/usr/ # with this it adds x86_64-linux-gnu to the path when running cpack
cmake --build $build_dir 
cpack -V --config $build_dir/CPackConfig.cmake -B $build_dir/packaging 
mvPack

#---------- Python
source_dir=python/api
build_dir=$BASE_BUILD_DIR/$source_dir
cmake -S $source_dir -B $build_dir -DCMAKE_PREFIX_PATH=$BASE_BUILD_DIR/$source_dir || exit 1
cmake --build $build_dir || exit 1
cpack -V --config $build_dir/CPackConfig.cmake -B $build_dir/packaging/ || exit 1
mvPack
