3.0.1
-----

* Replaced jdom2 XML parser with standard JVM parser.

3.0.0
-----

* Packages start with eu.ill.cameo.
* Corrected responder and subscriber proxy ports to use loopback address.
* Configuration proxy ports can be overriden by command line.

2.2.0
-----

* Modularization.
* Added environment variables to stop and error executables.

2.1.0
-----

* Change of version of cameo-common.

2.0.3
-----

* Implemented the max number of applications for an application defined with an integer multiple attribute.
* Corrected the check of the max global number of running applications.


2.0.2
-----

* Corrected logger level to take into account FINE and FINEST values.
* Added application name in some exceptions.

2.0.1
-----

* Managing bad requests.

2.0.0
-----

* Messaging architecture reviewed to support proxies for responders and publishers.
* Implementation of the linked apps i.e. stop an app when its parent dies.
* UNKNOWN state renamed into NIL.
* Use of $PID is deprecated.

1.1.0
-----

* Added support for variables in environment files.

1.0.3
-----

* Changed default value for log level from FINE to INFO.

1.0.2
-----

* Removed useless and confusing test "application.hasToBeKilled()" in LifecycleApplicationThread.onTermination() when processing exit value: In some cases the error was not propagated.

1.0.1
-----

* Added test on the operating system to add \ character before " on Windows: the JSON string passed as info argument cannot be parsed otherwise.

1.0.0
-----

* Replaced protobuf with JSON.
* In StreamApplicationThread, replaced reader.readLine() by a specialised readCharacters() method to manage properly the input streams (output from the cameo server, input from the process).
* Added server version request.
* Reviewed log messages.
* Added the argument --log-console in Server main.
* Added storage requests and send key value events.
* Send the exit code in the status events.
* Added connect with id request.
* Added sync stream request. Moved the creation of the stream thread at an earlier location.
* The class PortManager is responsible for managing the ports.
* Renamed some requests with v0 suffix to indicate they will disappear.
* Implemented new port requests.
* Argument info is passed in JSON format.
* Removed the 'retries' attribute of an application config.
* Reviewed 'stopping\_time' meaning: is used only when a stop handler is defined (code or executable). Otherwise if a stop is requested, a kill is applied.
* Added 'info\_arg' and 'output\_stream' attributes, 'pass_info' and 'stream' are kept. 
* Removed the 'debug' attribute from config.
* Added the 'log\_level' attribute in config.
* In config, enabled 'multiple'=yes and 'output\_stream'=yes.
* In config, 'output\_stream'=yes by default, even if 'multiple'=yes.

0.1.8
-----

* Cameo messages updated for protobuf.

0.1.7
-----

* If host="IP" the IP address is used for the endpoint.

0.1.6
-----

* Refined endpoint definition: if hostname is not available, try the IP address.
* Reviewed the log in cameo.log: do not log FINE messages.

0.1.5
-----

* Part of a maven multimodule project.
* Updated all cameo dependencies.

0.1.4
-----

* Depends on cameo-com.

0.1.3
-----

* Replaced call to System.gc() by a sleep to correct the memory leak since Java 9.

0.1.2
-----

* Replaced the profiles by submodules for jeromq and jzmq.
* Updated to Java 9 and using new features from the Process API to better follow the unmanaged applications.

0.1.0
-----

* JZMQ support.
* Implementation of unmanaged applications.
* Max applications property is now restricted to the set of application instances with the same name and not the entire set.
* Implemented the possibility to kill an application that is stopping.

0.0.3
-----

* Changed the loading of the file referenced by 'environment'.
* It is impossible to request two port with the same name without an error.
* Moved the basic tests to the cameo-tests project.

0.0.2
-----

* Implemented shutdown hook to kill forcibly the running applications. This is necessary when the server is run as daemon and is stopped to ensure that all applications are killed too.
* Added the attribute 'environment' in the 'application' tag to set up environment variables. The value must refer a readable properties file that contains the list of variables with their associated value. A default file with name <application name>-environment.properties is loaded if it exists. 