The source package comes up with examples in the different languages. The current page is showing how to run them in Linux.  
The end of the page shows rapidly how to run on Windows.


## Setup

### Linux

The Java, C++ and Python examples can be run using either *jzmq* or *jeromq*.

Open a shell and go to the CAMEO **root** directory.

Set the *PATH* variable so that the C++ programs are found:
```
export PATH=build/examples/cpp:build/cpp/proxy:$PATH
```

If necessary, set the *PYTHONPATH* variable so that the *cameopy* module is found e.g.:
```
export PYTHONPATH=/path/to/install/lib/python3/dist-packages:$PYTHONPATH
```
You may have to verify that *python* is defined (install *python-is-python3* package on Ubuntu).


Select *jzmq* **or** *jeromq* Java library.

Case *jzmq*:
```
export CLASSPATH=examples/java/jzmq/target/cameo-examples-jzmq-full.jar
```

You shall define the variable *LD_LIBRARY_PATH* so that *libjzmq.so* is found:
```
export LD_LIBRARY_PATH=/path/to/libjzmq
```
Start the cameo server with the examples configuration:
```
java fr.ill.ics.cameo.server.Server examples/config.xml --log-console
```

In each new shell, create the alias for *cmo*:
```
alias cmo="java -jar java/console/jzmq/target/cameo-console-jzmq-2.2.0-full.jar"
```

Case *jeromq*:
```
export CLASSPATH=examples/java/jeromq/target/cameo-tests-jeromq-full.jar
```

In each new shell, create the alias for *cmo*:
```
alias cmo="java -jar java/console/jeromq/target/cameo-console-2.2.0-full.jar"
```

### Windows

Open a standard prompt and go to the CAMEO **root** directory. Then set the *PATH*:
```
set PATH=build\examples\cpp\Release;build\cpp\proxy\Release;%PATH%
```

Set the *CLASSPATH* to use *jeromq*:
```
set CLASSPATH=tests\java\jeromq\target\cameo-tests-jeromq-full.jar
```

If necessary, set the *PYTHONPATH* variable so that the *cameopy* module is found e.g.:
```
set PYTHONPATH=build\python\api\Release;%PYTHONPATH%
```

Run the server:
```
java fr.ill.ics.cameo.server.Server examples\config.xml --log-console
```

In each new prompt or shell, create the alias for *cmo*:
```
doskey cmo=java -jar java\console\jeromq\target\cameo-console-jeromq-2.2.0-full.jar $*
```

## Requester/Responder

### Java

The requester/responder in Java can be tested.

Start the responder app in a new shell:  
```
cmo -p 11000 exec responder-java
```
Execute the requester app in a new shell:  
```
cmo -p 11000 exec requester-java tcp://localhost:11000 java "a message" 10
```
The requester app is sending 10 requests to the responder, receives the responses and then stops. Notice that the responder app also stops. Indeed the requester app stops itself the responder app.

Now relaunch the requester app without relaunching the responder app. The requester app is also sending and receiving messages from the responder app. Indeed the responder app has been started by the requester app because it was not running.

This example shows that by a single entry point (the console), the requester and responder apps start and communicate without losing any request and terminate synchronously.

Code is available:

* [Java Responder](https://code.ill.fr/cameo/cameo/-/blob/master/examples/java/src/fr/ill/ics/cameo/examples/ResponderApp.java)
* [Java Requester](https://code.ill.fr/cameo/cameo/-/blob/master/examples/java/src/fr/ill/ics/cameo/examples/RequesterApp.java)


### C++

The requester/responder in C++ can be tested in the same way by replacing *java* with *cpp*.
Start the responder in a shell:  
```
cmo -p 11000 exec responder-cpp
```
Execute the requester app in a new shell:  
```
cmo -p 11000 exec requester-cpp tcp://localhost:11000 cpp "a message" 10
```
Same comments as for Java.

Code is available:

* [C++ Responder](https://code.ill.fr/cameo/cameo/-/blob/master/examples/cpp/src/ResponderApp.cpp)
* [C++ Requester](https://code.ill.fr/cameo/cameo/-/blob/master/examples/cpp/src/RequesterApp.cpp)


### Python

The requester/responder in Python can be tested in the same way by replacing *cpp* with *python*.
Start the responder in a shell:  
```
cmo -p 11000 exec responder-python
```
Execute the requester app in a new shell:  
```
cmo -p 11000 exec requester-python tcp://localhost:11000 python "a message" 10
```
Same comments as for Java.

Code is available:

* [Python Responder](https://code.ill.fr/cameo/cameo/-/blob/master/examples/python/src/responderapp.py)
* [Python Requester](https://code.ill.fr/cameo/cameo/-/blob/master/examples/python/src/requesterapp.py)

### Mixing languages

You can mix the languages e.g. *cpp* with *python*:
```
cmo -p 11000 exec requester-python tcp://localhost:11000 cpp "a message" 10
```

### Remote execution

You can also execute the last example in three different computers:

```
cmo -e tcp://computer-a:11000 exec requester-python tcp://computer-b:11000 cpp "a message" 10
```

The console *cmo* application is executing *requester-python* on *computer-a* which interacts with *responder-cpp* executing on *computer-b*.

Here again, this example shows that by a single entry point (the console), the requester and responder apps start and communicate on two computers without losing any request and terminate synchronously.

## Publisher/Subscriber

### Java

The publisher/subscriber in Java can be tested.
Start the publisher app in a new shell:  
```
cmo -p 11000 exec publisher-java
```
Execute the subscriber app in a new shell:  
```
cmo -p 11000 exec subscriber-java tcp://localhost:11000 java
```

The publisher app is waiting for 1 subscriber before starting to send messages.
The subscriber app connects to the publisher app which triggers the sending of the messages.
The subscriber app is killed with *ctl-c* but the publisher app is still running so that you have to kill it also with *ctl-c*.

Now relaunch the subscriber app without relaunching the publisher app. The publisher is started in background and the messages are sent from the publisher to the requester.
If you kill the subscriber app with *ctl-c* then the publisher is also killed. That is because the publisher app is linked to its starter. You can also properly stop the subscriber app. To do it type *shift+s* in the console and press enter. In that case the subscriber app is stopping the publisher app which terminates properly.

This example shows that by a single entry point (the console), the subscriber and publisher apps start and communicate without losing any message and terminate synchronously.

Code is available:

* [Java Publisher](https://code.ill.fr/cameo/cameo/-/blob/master/examples/java/src/fr/ill/ics/cameo/examples/PublisherApp.java)
* [Java Subscriber](https://code.ill.fr/cameo/cameo/-/blob/master/examples/java/src/fr/ill/ics/cameo/examples/SubscriberApp.java)


### C++

The publisher/subscriber in C++ can be tested in the same way by replacing *java* with *cpp*.
Start the publisher in a shell:  
```
cmo -p 11000 exec publisher-cpp
```
Execute the subscriber app in a new shell:  
```
cmo -p 11000 exec subscriber-cpp tcp://localhost:11000 cpp
```
Same comments as for Java.

Code is available:

* [C++ Publisher](https://code.ill.fr/cameo/cameo/-/blob/master/examples/cpp/src/PublisherApp.cpp)
* [C++ Subscriber](https://code.ill.fr/cameo/cameo/-/blob/master/examples/cpp/src/SubscriberApp.cpp)


### Python

The publisher/subscriber in Python can be tested in the same way by replacing *java* with *python*.
Start the publisher in a shell:  
```
cmo -p 11000 exec publisher-python
```
Execute the subscriber app in a new shell:  
```
cmo -p 11000 exec subscriber-python tcp://localhost:11000 python
```
Same comments as for Java.

Code is available:

* [Python Publisher](https://code.ill.fr/cameo/cameo/-/blob/master/examples/python/src/publisherapp.py)
* [Python Subscriber](https://code.ill.fr/cameo/cameo/-/blob/master/examples/python/src/subscriberapp.py)


### Mixing languages

You can mix the langagues e.g. *cpp* with *python*:
```
cmo -p 11000 exec subscriber-python tcp://localhost:11000 cpp
```

### Remote execution

You can also execute the last example in three different computers:

```
cmo -e tcp://computer-a:11000 exec subscriber-python tcp://computer-b:11000 cpp
```

The console *cmo* application is executing *subscriber-python* on *computer-a* which interacts with *publisher-cpp* executing on *computer-b*.

Here again, this example shows that by a single entry point (the console), the subscriber and publisher apps start and communicate on two computers without losing any request and terminate synchronously.



