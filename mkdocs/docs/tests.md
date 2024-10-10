# Tests

## Run the tests

### Linux

Go to the CAMEO root directory and execute:

```
./tests/run
```

The tests executed will depend on the components build.


### Windows

Go to the CAMEO root directory and execute:

```
tests\run
```

The tests executed will depend on the components build.




## Run specific tests

### Linux setup

Go to the CAMEO root directory, open a shell, run *tests/run setup* and set the paths as it is done.

### Windows setup

Go to the CAMEO root directory, open a shell, run *tests\run setup* and set the paths as it is done.

### Run

Run all the tests with 10 iterations for each individual test:  
``` 
java eu.ill.cameo.test.TestSelector all 10
```
Start only the java tests:
```
java eu.ill.cameo.test.TestSelector java 10
```
Start only the C++ tests:
```
java eu.ill.cameo.test.TestSelector cpp 10
```
Start only the Python tests:
```
java eu.ill.cameo.test.TestSelector python 10
```
Display the available tests:
```
java eu.ill.cameo.test.TestSelector
```
	
Start a specific test:
```
java eu.ill.cameo.test.TestSelector testsimplejava 100
```

### Test an unregistered application

Test the Java unregistered application. Start the server in a specific shell:
```
java eu.ill.cameo.server.Server tests/java/tests.xml --log-console
```
Start the application:
```
java eu.ill.cameo.test.Stop "{\"name\":\"stop\", \"server\":\"tcp://localhost:11000\"}"
```

Then kill the application that should disappear from the list of applications.

Test the C++ unregistered application:
```
stop "{\"name\":\"stop\", \"server\":\"tcp://localhost:11000\"}"
```

### Test the attachment to a remote CAMEO server

Test the app connected to a remote server. Start the server in a specific shell:
```
java eu.ill.cameo.server.Server tests/java/tests.xml --log-console
```
Start the first application on the same computer:
```
java -jar java/console/jzmq/target/cameo-console-jzmq-3.0.0-full.jar -p 11000 exec testremoteservercpp
```

Start the second application on a second computer by passing the endpoint of the first computer:
```
./build/tests/cpp/remoteserver tcp://computer:11000
```
