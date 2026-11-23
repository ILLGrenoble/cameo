#!/bin/bash

expectedArgs=1

if [ $# -lt $expectedArgs ]
then
	echo "Usage: generate.sh <os name>"
	exit 1
fi

osName=$1

mkdir -p output
mkdir -p sif
mkdir -p releases

apptainer run --containall --bind ./output:/output sif/$osName.sif

cd output/packages/

for file in *.deb
do
	echo "Found $file"
done

cd ../..

suffix=".deb"
prefix=${file%%$suffix*}

debName=$prefix.deb
outDebName=$prefix-$osName.deb

mv output/packages/$debName releases/$outDebName
rm -fr output

echo "Generated $outDebName"
