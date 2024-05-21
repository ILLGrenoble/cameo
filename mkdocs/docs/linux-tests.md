# Setup

The Java, C++ and Python tests can be run using either *jzmq* or *jeromq*.

Open a shell and go to the root CAMEO directory:

Set the *PATH* variable so that the C++ programs are found:
```
export PATH=build/tests/cpp:build/cpp/proxy:$PATH
```

If necessary, set the *PYTHONPATH* variable so that the *cameopy* module is found.
The path is dependent on the system e.g.:
```
export CAMEO_PATH=<cameo-install-path>
export PYTHONPATH=$CAMEO_PATH/lib/python3/dist-packages:$PYTHONPATH
```

Select *jzmq* **or** *jeromq* Java library.

Case *jzmq*:
```
export CLASSPATH=tests/java/jzmq/target/cameo-tests-jzmq-full.jar
```

You shall define the variable *LD_LIBRARY_PATH* so that *libjzmq.so* is found:
```
export LD_LIBRARY_PATH=<path-to-libjzmq>
```

Case *jeromq*:
```
export CLASSPATH=tests/java/jeromq/target/cameo-tests-jeromq-full.jar
```

# Run the tests

Run all the tests with 10 iterations for each individual test:  
``` 
java fr.ill.ics.cameo.test.TestSelector all 10
```
Start only the java tests:
```
java fr.ill.ics.cameo.test.TestSelector java 10
```
Start only the C++ tests:
```
java fr.ill.ics.cameo.test.TestSelector cpp 10
```
Start only the Python tests:
```
java fr.ill.ics.cameo.test.TestSelector python 10
```
Display the available tests:
```
java fr.ill.ics.cameo.test.TestSelector
```
	
Start a specific test:
```
java fr.ill.ics.cameo.test.TestSelector testsimplejava 100
```

Test the Java unmanaged application:
Start the server in a specific shell
```
java fr.ill.ics.cameo.server.Server tests/java/tests.xml
```
Start the application:
```
java fr.ill.ics.cameo.test.Stop "{\"name\":\"stop\", \"server\":\"tcp://localhost:11000\"}"
```

Then kill the application that should disappear from the list of applications.

Test the C++ unmanaged application
```
stop "{\"name\":\"stop\", \"server\":\"tcp://localhost:11000\"}"
```

