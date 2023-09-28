2.2.0 (NP)
-----

* Minor review of exceptions.
* Renamed PROCESSING_ERROR into PROCESSING_FAILURE.
* Added the checkApp feature to Requester and Subscriber classes which implied some changes in their current implementation.
  WARNING: Create Subscriber with checkApp = true to cancel receive() automatically as in the previous version.
* Renamed getActualState() methods into getState().
* Modularization.
* Publisher with synchronized subscribers.
* Coms classes options outside create().
* Requester/Responder reimplemented with dealer/router sockets to allow multiple response.
* Added connect() in App to facilitate concurrent process.
* Default ZeroMQ implementation is JZMQ.
  WARNING: Change pom.xml in dependent applications.

2.1.1
-----

* Requester and subscriber init() can timeout.
* Added isReady() and isTerminated() to parent class of coms classes.
* Requester and subscriber init() can be canceled.
* If the first init() of an object succeeds i.e. the object is ready then the second call immediately returns.

2.1.0
-----

* Change of version of cameo-common.
* Corrected timeout bug with the Requester: timeout was reset to 0 if setTimeout() was called before init().

2.0.3
-----

* Do not block Requester and Subscriber if the remote application terminated before the registration of the key getter.

2.0.2
-----

* App.Config replaces hasSingleInstance() with getMultiple().

2.0.1
-----

* Added timeout parameter to connectToStarter() and connectToRequester() methods.

2.0.0
-----

* Messaging architecture reviewed to support proxies for responders and publishers.
* Linked applications.
* Added interfaces IObject, ITimeoutable, ICancelable.
* Renamed Instance into App, etc.
* Use of init() for Server and coms classes.
* Requester/Responder basic reimplementation.
* Requester/Responder multi reimplementation.
* Publisher/Subscriber reimplementation.
* New port strategy for coms classes.
* InitException replaces creation exceptions.
* Removed exists() from App and throw an exception if the application does not exist.
* One ZeroMQ context shared between all the servers.

1.1.0
-----

* Requester, Responder, Request classes out of Application, implemented with key values.

1.0.0
-----

* Replaced protobuf with JSON.
* Added Server.getVersion() which returns the server version.
* Added Output.isEndOfLine() which returns true if the message terminates with an end of line.
* Removed Configuration.getRetries().
* Added storage methods.
* Added Instance.getPastStates().
* Added Instance.getExitCode().
* Filter OutputStreamSocket on application id.
* Added Server.connect() with id.
* Output stream is synced.
* Added Instance.waitFor(KeyValue) to wait for a key value event.
* Added Com classes for port and storage requests.
* The methods getEndpoint() now return an Endpoint object.
* Argument info passed in JSON format.
* Added optional stopping time in This.handleStop() method.

0.1.9
-----

* Cameo messages updated for protobuf.

0.1.8
-----

* Simplified EventThread by only forwarding events. 


0.1.7
-----

* Optimized socket creation by keeping alive all the sockets. 

0.1.6
-----

* Corrected implementation of Request.get() and Requester.send() to be coherent with C++ using protobuf encoding of strings.

0.1.5
-----

* Added cancel(), isCanceled(), isEnded() methods to classes with blocking methods.
* Reviewed cancel strategy for EventStreamSocket and OutputStreamSocket.
* Removed Instance.now() method and added Instance.getActualState() and Instance.getLastState(). 

0.1.4
-----

* Part of a maven multimodule project.
* Updated all cameo dependencies.

0.1.3
-----

* Depends on cameo-com.

0.1.2
-----

* Replaced the profiles by submodules for jeromq and jzmq.
* Enabled to define more than one requester on the same responder in one Application instance.
* Updated to Java 9 and using new features from the Process API to better follow the unmanaged applications.

0.1.0
-----

* Server constructor throws runtime exception SocketException if the connect call from JeroMQ throws an exception.
* Implemented connection checker by creating a ConnectionChecker class.
* Removed StateException, setRunning now returns a boolean.
* Added InvalidArgumentException to replace invalid_argument exception.
* Added SocketException thrown when the Server connect fails.
* Changed the interface for the stop handler.
* JZMQ support.
* Implementation of unmanaged applications.
* Added two binary parts message for publisher/subscriber and requester/responder.
* Refined internal requester port name by adding the responder id.
* Added connectToRequester method to Request.