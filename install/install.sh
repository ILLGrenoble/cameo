#!/bin/sh

if [ $# -lt 2 ]
then
	echo "Usage: install.sh <version> <install directory>"
	exit 1
fi

version=$1
prefixInstallDir=$2

mkdir -p $prefixInstallDir/lib
mkdir -p $prefixInstallDir/lib/java
mkdir -p $prefixInstallDir/bin
libDir=$prefixInstallDir/lib/java
binDir=$prefixInstallDir/bin

libName="cameo-console-"$version"-full.jar"
targetLibName="../target/$libName"

if [ ! -e "$targetLibName" ]
then
	echo "The version $1 does not exist."
	exit 1
fi

cp $targetLibName $libDir 

echo "Installed $libName into $libDir"

fileName="$binDir/cmo"
echo "#!/bin/sh" > $fileName
echo "java -jar $libDir/$libName \$@" >> $fileName

chmod 755 $fileName

echo "Installed cmo into $binDir"