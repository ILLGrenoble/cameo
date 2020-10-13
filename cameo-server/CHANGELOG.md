1.0.0
-----

* Replaced protobuf by JSON.
* In StreamApplicationThread, replaced reader.readLine() by a specialised readCharacters() method to manage properly the input streams (output from the cameo server, input from the process).
* Removed the 'retries' attribute of an application config.
* Added server version request.
* Reviewed log messages.
* Removed the 'debug' attribute from config.
* Added the 'log_level' attribute in config.
* Added the argument --log-console in Server main.
* Added storage requests.
* Send the exit code in the status events.
* Enabled multiple=yes and stream=yes.

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