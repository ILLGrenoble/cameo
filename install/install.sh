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

libName="cameo-server-"$version"-full.jar"
targetLibName="../target/$libName"

if [ ! -e "$targetLibName" ]
then
	echo "The version $1 does not exist."
	exit 1
fi

cp $targetLibName $libDir 

libNoVersionName="cameo-server-full.jar"
rm $libDir/$libNoVersionName
ln -s $libDir/$libName $libDir/$libNoVersionName

echo "Installed $libName into $libDir"

fileName="$binDir/cmo-server"
echo "#!/bin/sh" > $fileName
echo "java -jar $libDir/$libNoVersionName \$@" >> $fileName

chmod 755 $fileName

echo "Installed cmo-server into $binDir"