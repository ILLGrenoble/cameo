0.1.2
-----

* Replaced the profiles by submodules for jeromq and jzmq.

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