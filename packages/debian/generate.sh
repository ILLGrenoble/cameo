#!/bin/bash

expectedArgs=2

if [ $# -lt $expectedArgs ]
then
	echo "Usage: generate.sh <version> <os name>"
	exit 1
fi

version=$1
osName=$2

mkdir -p output
mkdir -p tmp
mkdir -p sif
mkdir -p releases

apptainer run --containall --bind ./output:/output --bind ./tmp:/tmp sif/$osName.sif

mv output/packages/ releases/$osName
rm -fr output tmp

archiveName="cameo-$version-$osName.zip"

zip -r $archiveName releases/$osName

echo "Generated packages for $osName"
