The Cameo C++ API sources can be compiled on different platforms.

# Automake/Libtool

To compile as a developer:

	./build.sh
	cd build
	../configure OPTIONS
	make proto
	make
	make install (by root if necessary)

# Visual Studio 2015

Install the following components:

### IDE

Download and install Visual C++ Community. You will maybe have to repair the installation if it fails to install.
 
### ZeroMQ

Download the installer from the [page] (http://zeromq.org/distro:microsoft-windows).  
    
### Protocol Buffers

Download the compiler pre-built 2.6.1.  
Change the path so that protoc is accessible from the command line.  
Compile Protocol Buffers 2.6.1 with VS 2015.  
* Compile the projects in vsprojects (implies their migration)  
* Add _SILENCE_STDEXT_HASH_DEPRECATION_WARNINGS in preprocessor definitions.

### Boost C++
  
Install Boost 1.59 with the installer from the [page] (https://sourceforge.net/projects/boost/files/boost-binaries/).  
  
### Cameo

Compile the Protocol Buffers files:  

	cd src/proto  
	protoc -I=. --cpp_out=. Messages.proto  
  
Open the project in the msvc/cameo-api-cpp.  
Modify the paths of ZeroMQ, Protocol Buffers and Boost.   
Generate the library which is a static library.  
  
Notes:
Boost threads do not work with the Matlab 2015b engine because Matlab depends on an older Boost version.