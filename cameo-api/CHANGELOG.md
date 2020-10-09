1.0.0
-----

* Replaced protobuf by JSON.
* Added Server.getVersion() which returns the server version.
* Added Output.isEndOfLine() which returns true if the message terminates with an end of line.
* Removed Configuration.getRetries().
* Added storage methods: This.storeKeyValue(), This.getKeyValue(), This.removeKey(), Instance.getKeyValue().
* Added Instance.getPastStates().  

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