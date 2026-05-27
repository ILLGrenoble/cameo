#!/bin/bash

expectedArgs=1

if [ $# -lt $expectedArgs ]
then
	echo "Usage: buildsif.sh <os name>"
	exit 1
fi

osName=$1

apptainer build $osName.sif $osName.def