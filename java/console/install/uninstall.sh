#!/bin/sh

if [ $# -lt 2 ]
then
	echo "Usage: uninstall.sh <version> <install directory>"
	exit 1
fi

version=$1
prefixInstallDir=$2

libDir=$prefixInstallDir/lib/java
binDir=$prefixInstallDir/bin
libName="cameo-console-"$version"-full.jar"
targetLibName="$libDir/$libName"

rm -f "$targetLibName"
echo "Deleted $targetLibName."

rm -f "$binDir/cmo"
echo "Deleted $binDir/cmo."