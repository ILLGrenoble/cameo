2.0.3 (not released)
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