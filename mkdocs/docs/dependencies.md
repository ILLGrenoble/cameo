JZMQ or JeroMQ?
---------------

CAMEO provides three Java projects and a C++ project. ZeroMQ is written in C and using it in Java projects can be done:

* Use the Java binding [JZMQ](https://github.com/zeromq/jzmq).
* Use the pure Java implementation [JeroMQ](https://github.com/zeromq/jeromq).

CAMEO can use both as they follow the same interface, so that they can be easily replaced. However some differences exist:

* JZMQ has better performances than JeroMQ.
* JZMQ is more difficult to configure as the dynamic library of ZeroMQ must be accessible.
* JZMQ is no longer developped. It is thus preferrable to use JeroMQ if no blockings are encountered.


JZMQ compilation
----------------

JeroMQ does not require any compilation because the jar is downloaded from a Maven repository. However if the JZMQ Java jar is downloaded from a Maven repository, the C dynamic library must be compiled if not accessible from a package.

* On Debian, we use the package *libzmq-jni*.
* On Windows 7 64bits, we compiled JZMQ using Visual Studio 2015 as explained in this [page](https://gerrydevstory.com/2015/04/27/zeromq-windows-java-binding-building-jzmq-dll-and-zmq-jar-using-visual-studio-2012/) but we needed to add the configuration x64 for the project JZMQ.

JSON libraries
--------------

In the Java API, we currently use [json-simple](https://github.com/fangyidong/json-simple) that is very simple but performant. Other libraries are less performant so that we decided to keep it.