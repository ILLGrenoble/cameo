#!/bin/bash

if [ $# -lt 1 ]
then
	echo "Usage: install.sh <version> [lib dir] [bin dir]"
	exit 1
fi

version=$1

libDir=${2:-/usr/local/share/java/}
binDir=${3:-/usr/local/bin/}
jzmq="-jzmq"

install_script_dir=`dirname $0`

mkdir -p "$libDir" || {
    echo "[ERROR] Maybe you don't have the permission to create the directory" >> /dev/stderr
    exit 1
}
mkdir -p "$binDir"

libName=cameo-server${jzmq}-${version}-full.jar
targetLibName=cameo-server$jzmq/target/$libName

if [ ! -e "$targetLibName" ]
then
    echo "File $targetLibName not found" >> /dev/stderr
    echo "The version $1 does not exist." >> /dev/stderr
    exit 1
fi

cp "$targetLibName" "$libDir" ||{
    echo "[ERROR] Maybe you don't have the permission to write in the $libDir directory" >> /dev/stderr
    exit 1
}


libNoVersionName="cameo-server.jar"
rm "$libDir/$libNoVersionName"
ln -s "$libDir/$libName" "$libDir/$libNoVersionName"

echo "Installed $libName into $libDir"
chmod +r $libDir/$libName
#############################################
fileName="$binDir/cameo-server"
echo "#!/bin/sh" > "$fileName"
echo "java -jar \"$libDir/$libNoVersionName\" \$@" >> "$fileName"

chmod 755 "$fileName"

echo "Installed cameo-server into $binDir"


#############################################
fileName=$binDir/cmo
version=1.0.0
cat > $fileName <<EOF
#!/bin/sh
java -jar "$libDir/cameo-console${jzmq}-${version}-full.jar" \$@
EOF
chmod 755 $fileName
echo "Installed cameo-console into $fileName"

fileName=cameo-console$jzmq-$version-full.jar

cp cameo-console$jzmq/target/$fileName $libDir
chmod +r $libDir/$fileName
echo "Installed $fileName into $libDir"
