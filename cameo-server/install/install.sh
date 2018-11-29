#!/bin/sh

if [ $# -lt 1 ]
then
	echo "Usage: install.sh <version> [lib dir] [bin dir]"
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

mkdir -p "$libDir"
mkdir -p "$binDir"

libName="cameo-server-"$version"-full.jar"
targetLibName="../target/$libName"

if [ ! -e "$targetLibName" ]
then
	echo "The version $1 does not exist."
	exit 1
fi

cp "$targetLibName" "$libDir"

libNoVersionName="cameo-server.jar"
rm "$libDir/$libNoVersionName"
ln -s "$libDir/$libName" "$libDir/$libNoVersionName"

echo "Installed $libName into $libDir"

fileName="$binDir/cameo-server"
echo "#!/bin/sh" > "$fileName"
echo "java -jar \"$libDir/$libNoVersionName\" \$@" >> "$fileName"

chmod 755 "$fileName"

echo "Installed cameo-server into $binDir"
