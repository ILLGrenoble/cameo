#!/bin/sh

if [ $# -lt 1 ]
then
	echo "Usage: uninstall.sh <version> [lib dir] [bin dir]"
	exit 1
fi

version=$1
libDir=$2
binDir=$3

if [ "$libDir" = "" ]
then
	libDir="/usr/share/java/"
fi

if [ "$binDir" = "" ]
then
	binDir="/usr/local/bin/"
fi

libName="cameo-server-"$version"-full.jar"
targetLibName="$libDir/$libName"

rm -f "$targetLibName"
echo "Deleted $targetLibName."

rm -f "$binDir/cameo-server"
echo "Deleted $binDir/cameo-server."
