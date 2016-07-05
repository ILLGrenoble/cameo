The Cameo C++ API sources can be compiled on different platforms.

* Automake/Libtool

To compile as a developer:

	./build.sh
	cd build
	../configure OPTIONS
	make proto
	make
	make install (by root if necessary)

* Visual Studio 2015

Install the following components:

** Install Visual C++ Community

	Maybe repair the installation if it fails
 
** ZeroMQ

	Download the installer 
    
** Protocol Buffers

	Download the compiler pre-built 2.6.1
	Change the path so that protoc is accessible from the command line
	Compile Protocol Buffers 2.6.1 with VS 2015
    	Use the projects in vsprojects (implies their migration)
    	Add _SILENCE_STDEXT_HASH_DEPRECATION_WARNINGS in preprocessor definitions
    Install Boost 1.59 with the installer
  
Notes:
** Boost threads do not work with the Matlab 2015b engine because Matlab depends on an older Boost version.