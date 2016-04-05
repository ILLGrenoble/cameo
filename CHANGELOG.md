0.0.2
-----

* Implemented shutdown hook to kill forcibly the running applications. This is necessary when the server is run as daemon and is stopped to ensure that all applications are killed too.
* Added the attribute 'environment' in the 'application' tag to set up environment variables. The value must refer a readable properties file that contains the list of variables with their associated value. A default file with name <application name>-environment.properties is loaded if it exists. 