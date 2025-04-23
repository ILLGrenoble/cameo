3.0.3 not published
-----

* Default dependency to jeromq.

3.0.2
-----

* Reimplemented the host="IP" feature in server so that we do not call InetAddress.getLocalHost().getHostAddress() but do an iteration over the network interfaces.

3.0.1
-----

* Replaced jdom2 XML parser with standard JVM parser in server.

3.0.0
-----

* Java packages start with eu.ill.cameo.
* Corrected server responder and subscriber proxy ports to use loopback address.
* Server configuration proxy ports can be overriden by command line.
* Exports for Windows DLL.

2.2.0
-----

* Renamed constant PROCESSING_ERROR into PROCESSING_FAILURE.
* Added the checkApp feature to Requester and Subscriber classes which implied some changes in their current implementation.
  WARNING: Create Subscriber with checkApp = true to cancel receive() automatically as in the previous version.
* Java modularization.
* Coms classes options outside create().
* Requester/Responder reimplemented with dealer/router sockets to allow multiple response.
* Default ZeroMQ implementation is JZMQ in Java API.
  WARNING: Change maven pom.xml in dependent applications.
* Directory review for server and console java projects.
* Subscriber receive() can timeout.
* CMake review.

2.1.1
-----

* Requester and subscriber init() can timeout.
* Added isReady() and isTerminated() to parent class of coms classes.
* Requester and subscriber init() can be canceled.
* If the first init() of an object succeeds i.e. the object is ready then the second call immediately returns.

2.1.0
-----

* Modification of the implementation of StringId. Coms classes are not compatible with older versions.
* Corrected timeout bug with the Requester classes: timeout was reset to 0 if setTimeout() was called before init().

2.0.5
-----

* In the APIs, do not block Requester and Subscriber if the remote application terminated before the registration of the key getter.

2.0.4
-----

* Implemented the max number of applications for an application defined with an integer multiple attribute in config file.
* Corrected the check of the max global number of running applications.
* Display the max number of applications in console list result.

2.0.3
-----

* Added parameter timeout to connectToStarter() and connectToRequester() methods and functions.
* Corrected server logger level to take into account FINE and FINEST values.
* Added application name in some server exceptions propagated to client.

2.0.2
-----

* Corrected GIL management in python binding.

2.0.1
-----

* Corrected missing dependencies in cameo-api-cpp library.

2.0.0
-----

* Messaging architecture reviewed to support proxies for responders and publishers.
* Linked applications.
* Clean interface for implementing coms classes.
* Requester/Responder basic reimplementation.
* Requester/Responder multi implementation.
* Publisher/Subscriber reimplementation.
* New port strategy for coms classes.

1.0.0
-----

* Replaced protobuf with JSON.