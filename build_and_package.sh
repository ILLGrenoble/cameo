#!/bin/bash
set -euo pipefail

CPACK_GENERATORS="DEB;RPM"

BASE_BUILD_DIR=${1:-/dev/shm/cameo/}
[ "$BASE_BUILD_DIR" != "${BASE_BUILD_DIR#/}" ] || BASE_BUILD_DIR=$PWD/$BASE_BUILD_DIR

source_dir_cpp=cpp/api
packages_dir=${BASE_BUILD_DIR}/packages
mkdir ${packages_dir} -p
#mvn  install

function mvPack(){
	mv ${build_dir}/packaging/*.{rpm,deb} ${packages_dir}/
}

#--------------- JAVA
for source_dir in java/server-jzmq java/console-jzmq
do
	echo "COMPONENT: $source_dir"
	build_dir=$BASE_BUILD_DIR/$source_dir
	cmake -S $source_dir -B $build_dir/
	cmake --build $build_dir 
	cpack --config $build_dir/CPackConfig.cmake -B $build_dir/packaging/ -G $CPACK_GENERATORS
	mvPack
done




#---------- Server proxy (C++)
source_dir=cpp/proxy
build_dir=$BASE_BUILD_DIR/$source_dir
cmake -S $source_dir -B $build_dir -DCMAKE_INSTALL_PREFIX=/usr/ -DCMAKE_MODULE_PATH=$source_dir/../ # with this it adds x86_64-linux-gnu to the path when running cpack
cmake --build $build_dir 
cpack --config $build_dir/CPackConfig.cmake -B $build_dir/packaging -G $CPACK_GENERATORS
mvPack

#--------------- API
#---------- C++
build_dir=$BASE_BUILD_DIR/$source_dir_cpp
cmake -S $source_dir_cpp -B $build_dir -DCMAKE_INSTALL_PREFIX=/usr/ # with this it adds x86_64-linux-gnu to the path when running cpack
cmake --build $build_dir 
cpack --config $build_dir/CPackConfig.cmake -B $build_dir/packaging -G $CPACK_GENERATORS
mvPack

#---------- Python
source_dir=python/api
build_dir=$BASE_BUILD_DIR/$source_dir
cmake -S $source_dir -B $build_dir -DCMAKE_PREFIX_PATH=$BASE_BUILD_DIR/$source_dir_cpp
cmake --build $build_dir
cpack --config $build_dir/CPackConfig.cmake -B $build_dir/packaging/ -G $CPACK_GENERATORS
mvPack

