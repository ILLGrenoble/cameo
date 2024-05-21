# Getting started

We present a complete simple example where two CAMEO applications interact with a publisher/subscriber pattern.

## Code
We define:

* a C++ publisher application that publishes to a single subscriber.
* a Java subscriber application which starts the publisher application on the local CAMEO server and subscribes to it.

Code for the C++ Publisher application:
```cpp
#include <cameo/api/cameo.h>

int main(int argc, char *argv[]) {
	
    // Initialise the CAMEO application represented by This. 
    cameo::This::init(argc, argv);

    // Declare a publisher.
    std::unique_ptr<cameo::coms::Publisher> publisher;

    try {
        // Create the publisher with name "pub" that waits for a single subscriber.
        publisher = cameo::coms::Publisher::create("pub");
        publisher->setWaitForSubscribers(1);

        // Initialize the publisher. Synchronize with the subscriber.
        publisher->init();

        // Once here, we are sure that the subscriber is ready
        // and will be able to receive all the messages.
    }
    catch (const cameo::InitException& e) {
        // The publisher cannot be created.
        return 1;
    }

    // Set the state RUNNING.
    cameo::This::setRunning();

    // We can send data to the unique subscriber.
    publisher->send("hello");
    publisher->send("world");
    publisher->send("!");

    // Send the end of the stream.
    publisher->sendEnd();

    return 0;
}
```
Code for the Java Subscriber application:
```java
import fr.ill.ics.cameo.base.App;
import fr.ill.ics.cameo.base.Server;
import fr.ill.ics.cameo.base.This;
import fr.ill.ics.cameo.coms.Subscriber;

public class SubscriberApplication {

    public static void main(String[] args) {
		
        // Initialise the CAMEO application represented by This.	
        This.init(args);
				
        // Get the local CAMEO server (the one that started This).
        Server server = This.getServer();
		
        // Declare the subscriber.		
        Subscriber subscriber = null;
		
        try {
            // Start the publisher application with name "pubcpp"
            // and get a reference to the running application with the Instance object.
            App publisherApp = server.start("pubcpp");

            // Subscribe to the publisher with name "pub".
            Subscriber subscriber = Subscriber.create(publisherApp, "pub");

            // Initialize the subscriber.
            subscriber.init();
			
            // We are ready to receive data.
            while (true) {
                // Receive string encoded messages.
                String data = subscriber.receiveString();
                if (data != null) {
                    System.out.println("received " + data);
                }
                else {
                    break;
                }
            }
			
            // Finished to receive the data.
            // We can wait for the termination of the "pubcpp" application.
            int state = publisherApp.waitFor();

            // At this point, the "pubcpp" application is terminated and its terminal state is state.
        }
        catch (StartException e) {
            System.out.println("cannot start the publisher application");
        }
        catch (InitException e) {
            System.out.println("cannot initialize the subscriber");
        }
        finally {
            // Do not forget to terminate the subscriber and This in Java.
            subscriber.terminate();
            This.terminate();
        }
    }
}
```

You can notice the call to *start()* with the "pubcpp" name that is not already defined. It will done in the configuration file. Indeed define the code is not enough to run CAMEO applications. We need to configure a CAMEO server.

## Configuration
Here is a possible configuration file *config.xml*:
```xml
<config port="7000">
    <applications>
        <application name="pubcpp" log_directory="logs">
            <start executable="/path/to/pubcppapp"/>
        </application>
        <application name="subpubjava" log_directory="logs">
            <start executable="/usr/bin/java" args="-classpath /path/to/tests.jar test.SubscriberApplication"/>
        </application>	
    </applications>
</config>
```
A CAMEO server configuration file contains:

* The base port from which it is accessed i.e. 7000.
* The list of applications that can be started with their name, executable and other attributes. Each application is a mapping between a name and a command including the executable and the fixed arguments. This configuration file supposes:
  * The jar *tests.jar* contains the code of the Java application.

## Execution
First we must start the CAMEO server. This can be done directly in a shell:

```
cameo-server config.xml --log-console
```

Now we have to execute the *subpubjava* application as it is the entry point. That can be done by using the CAMEO console on the machine that runs the CAMEO server:

```
cmo exec subpubjava
```

If *serverhost* is the hostname of the machine running the CAMEO server, you can go on another machine and run *subpubjava* remotely:

```
cmo -e tcp://serverhost:7000 exec subpubjava
```

You have the first running example.
