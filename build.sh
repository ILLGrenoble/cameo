#!/bin/sh

if [ ! -d config ]
then
	mkdir config;
fi

libtoolize --force
aclocal -I config -I m4 --install
autoconf
automake --gnu --add-missing

if [ ! -d build ]
then
	mkdir build;
fi
