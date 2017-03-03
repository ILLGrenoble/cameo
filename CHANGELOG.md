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