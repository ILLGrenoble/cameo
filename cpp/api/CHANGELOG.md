2.1.2 (NP)
-----

* Minor review of exceptions.
* Renamed PROCESSING_ERROR into PROCESSING_FAILURE.
* Added the checkApp feature to Requester and Subscriber classes.

2.1.1
-----

* Requester and subscriber init() can timeout.
* Added isReady() and isTerminated() to parent class of coms classes.
* Requester and subscriber init() can be canceled.
* If the first init() of an object succeeds i.e. the object is ready then the second call immediately returns.

2.1.0
-----

* Modified StringId implementation to have the integer id at the end of the string.
* Corrected timeout bug with the Requester: timeout was reset to 0 if setTimeout() was called before init().

2.0.4
-----

* Do not block Requester and Subscriber if the remote application terminated before the registration of the key getter.

2.0.3
-----

* App.Config replaces hasSingleInstance() with getMultiple().


2.0.2
-----

* Added timeout parameter to connectToStarter() and connectToRequester() functions.

2.0.1
-----

* Corrected missing dependencies in dynamic library.

2.0.0
-----

* Messaging architecture reviewed to support proxies for responders and publishers.
* Linked applications.
* Added interfaces Object, Timeoutable, Cancelable.
* Renamed Instance into App, etc.
* Use of init() for Server and coms classes.
* Requester/Responder basic reimplementation.
* Requester/Responder multi reimplementation.
* Publisher/Subscriber reimplementation.
* New port strategy for coms classes.
* InitException replaces creation exceptions.
* Removed exists() from App and throw an exception if the application does not exist.
* One ZeroMQ context shared between all the servers.

1.2.0
-----

* Requester, Responder, Request, Publisher, Subscriber out of Application.h

1.1.1
-----

* Install path corrected for deb packages.

1.1.0
-----

* Removed Option enum and defined OUTPUSTREAM as const int.
* Added Instance::waitFor() and Instance::waitFor(int) for python binding.
* Implemented subscriber and requester with optional string result in receive() function.
* Implemented Instance::getResult() with optional string result.
* Implemented OutputStreamSocket::receive() with optional Output result.
* Instance::getOutputStreamSocket() now returns a unique_ptr<OutputStreamSocket>.

1.0.2
-----

* Removed JSON.h from include.
* Implemented InstanceArray as vector<unique_ptr<Instance>>.

1.0.1
-----

* Some minor changes to compile under Visual Studio 19.
* In Server constructor, moved retrieval of server version in try/catch to avoid exception when there is a timeout.

1.0.0
-----

* Replaced protobuf with JSON.
* Added Server::getVersion() which returns the server version.
* Added Output::isEndOfLine() which returns true if the message terminates with an end of line.
* Removed Configuration::getRetries().
* Added storage functions.
* Added Instance::getPastStates().
* Added Instance.getExitCode().
* Filter OutputStreamSocket on application id.
* Added Server::connect() with id.
* Output stream is synced.
* Added Instance::waitFor(KeyValue) to wait for a key value event.
* Added Com classes for port and storage requests.
* The functions getEndpoint() now return an Endpoint object.
* Argument info passed in JSON format.
* Added optional stopping time in This::handleStop() function.

0.3.3
-----

* In Request::connectToRequester(), create Server with inherited timeout.

0.3.2
-----

* Added Request::getRequesterEndpoint() function.

0.3.1
-----

* Refined timeout management
  - When RequestSocketImpl has timeout, linger is set to 100ms.
  - Request::setTimeout() propagates the timeout to the associated Server.
* RequestSocketImpl creates the socket and can reset it when a timeout occurs.
* Added Server(endpoint, timeout) constructor to avoid blocking when calling Server(endpoint) with an inacessible endpoint.

0.3.0
-----

* Implemented timeout in Request. Refined the timeout possibilities in RequestSocketImpl.

0.2.2
-----

* Only one EventStreamSocket is created for a Server. An EventThread is forwarding the Event objects to the EventListener objects.
* The zmq sockets are kept alive and not created for each request.

0.2.1
-----

* Added access to the output stream of an application.
* Added Instance.getLastState() and Instance.getActualState() functions.

0.2.0
-----

* Migration to C++11.

0.1.4
-----

* Adapted to gcc 6.3.0.

0.1.3
-----

0.1.2
-----

* Enabled to define more than one requester on the same responder in one Application instance.
* Removed zmq.hpp as it should be installed.
* Corrected some memory leaks by changing the return type of tryRequestWithOnePartReply.
* Better follow the unmanaged applications.

0.1.0
-----

* Corrected Server::isAvailable implementation when timeout is set.
* Implemented connection checker by creating a ConnectionChecker class.
* Removed StateException, setRunning now returns a boolean.
* Added InvalidArgumentException to replace invalid_argument exception.
* Added SocketException thrown when the Server connect fails.
* Renamed ERROR into FAILURE.
* Implementation of unmanaged applications.
* Implemented This static instance without pointer so that it is not necessary to call This::terminate() except if the destruction of the static instance is not automatic.
* Added two binary parts message for publisher/subscriber and requester/responder.
* Refined internal requester port name by adding the responder id.
* Added connectToRequester function to Request.