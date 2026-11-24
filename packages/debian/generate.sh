#!/bin/bash

expectedArgs=1

if [ $# -lt $expectedArgs ]
then
	echo "Usage: generate.sh <os name>"
	exit 1
fi

osName=$1

mkdir -p output
mkdir -p tmp
mkdir -p sif
mkdir -p releases

apptainer run --containall --bind ./output:/output --bind ./tmp:/tmp sif/$osName.sif

mv output/packages/ releases/$osName
rm -fr output tmp

echo "Generated packages for $osName"
