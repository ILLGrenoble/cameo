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

Test the Java unmanaged application:
Start the server in a specific shell
```
java eu.ill.cameo.server.Server tests/java/tests.xml --log-console
```
Start the application:
```
java eu.ill.cameo.test.Stop "{\"name\":\"stop\", \"server\":\"tcp://localhost:11000\"}"
```

Then kill the application that should disappear from the list of applications.

Test the C++ unmanaged application
```
stop "{\"name\":\"stop\", \"server\":\"tcp://localhost:11000\"}"
```

