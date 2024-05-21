Windows (outdated)
-------

The Java and C++ tests can be run using the jzmq-win-api-config.xml after having been compiled. Edit the win-environment.properties file to modify the path for the C++ executables. If you get the CAMEO binaries without compiling the sources, you need to install Visual C++ Redistributable.

You can get the *libzmq.dll* and *jzmq.dll* by downloading [zmq-win.zip](resources/zmq-win.zip).

We recommend to use a bash emulator for a better integration, however you must ensure that *libzmq.dll* and *jzmq.dll* are in the path.
```
set PATH=<libs-path>;%PATH%   
```
To run all the tests with 10 iterations for each individual test:  
Start all the tests
```
java -jar cameo-tests-jzmq\target\cameo-tests-jzmq-full.jar jzmq-win-api-config.xml all 10
```	
Display the available tests	
```
java -jar cameo-tests-jzmq\target\cameo-tests-jzmq-full.jar jzmq-win-api-config.xml
```	
Start a specific test
```
java -jar cameo-tests-jzmq\target\cameo-tests-jzmq-full.jar jzmq-win-api-config.xml startsimplejava 100
```	
Test the unmanaged application:  
Start the server in a specific shell
```
java -classpath cameo-tests-jzmq\target\cameo-tests-jzmq-full.jar fr.ill.ics.cameo.server.Server jzmq-win-api-config.xml
```
```
java -jar cameo-tests-jzmq\target\cameo-tests-jzmq-full.jar fr.ill.ics.cameo.test.TestStopApplication tcp://localhost:10000:stop
```
Then kill the application manually that should disappear from the list of applications. In command prompt
```
taskkill /pid PID /f
```

If the dependent libraries, i.e. libzmq.dll, jzmq.dll are in the same directory than cameo-tests-jzmq-0.0.1-full.jar java.library.path must be removed.
