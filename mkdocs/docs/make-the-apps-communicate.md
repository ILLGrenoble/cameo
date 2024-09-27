# Make the apps communicate

Controlling the CAMEO apps is already a form of communication between the apps. We show here the common ways to make them communicate by using the communication patterns aka coms and the return value.

The following examples will only show string messages however any kind of serialization can be used: binary serialization with *Protobuf* or ascii serialization with *JSON*.

## Function pattern
First and easiest way to pass data from an app to another is to use the function pattern. When an app terminates it is possible to set a return value that will be published to all the instance references. We can extend the Java application of *App2*:

```java
import eu.ill.cameo.base.This;

public class JavaApp {

    public static void main(String[] args) {
		
        // Initialize the CAMEO application represented by This.	
        This.init(args);
		
        // Infinite printing loop.
        int i = 0;
        while (true) {
            System.out.println("Printing " + i);
            try {
                Thread.sleep(100);
            }
            catch (InterruptedException e) {
            }
            i++;
        }

        // Set the string result.
        This.setResult("This is a result");

        This.terminate();	
    }
}
```

In C++, we can get the string result:
```cpp
#include <cameo/api/cameo.h>
#include <iostream>

int main(int argc, char *argv[]) {

    // Define the reference to the CAMEO server in B.	
    std::unique_ptr<cameo::Server> server = cameo::Server::create("tcp://B:7000");

    // Start the application "App2" and obtain a reference to the running app.
    std::unique_ptr<cameo::App> app2 = server.start("App2");

    // Wait for the end of the app and get the terminal state.
    cameo::state::Value state = app2->waitFor();

    // Get the result and display it.
    std::optional<std::string> result = app2->getResult();
    if (result.has_value()) {
        std::cout << "Result " << result.value() << std::endl;
    }
    else {
        std::cout << "No result" << std::endl;
    }

    return 0;
}
```

In Java and Python, you have the *getStringResult()* method and function to retrieve the string result. If you need to set and get a binary result, use the related *setResult()* and *getResult()* methods and functions.

Use the return value can be very helpful to use an app as a function. However it is **not recommended** to use it in those cases:

* The execution of the app is very short and it is executed lots of time.
* The data passed are big.  

Indeed in that case it is better to setup a requester/responder communication to avoid too many creation and destruction of processes and to avoid a network overload.

## Requester/Responder pattern

If you need to setup a request/response mechanism between different apps then use the provided requester/responder communication pattern.

An example of a responder in a C++ application registered as *ResApp* in the CAMEO server:
```cpp
try {
    // Create the basic responder with name "the-responder".
    std::unique_ptr<coms::basic::Responder> responder = coms::basic::Responder::create("the-responder");

    // Initialize the responder.
    responder->init();

    std::cout << "Created responder " << *responder << std::endl;

    // Loop on the requests.
    while (true) {			
        // Receive the simple request.
        std::unique_ptr<coms::basic::Request> request = responder->receive();
        if (!request) {
            std::cout << "Responder is canceled" << std::endl;
            break;
    }

    // Print the request data as string.
    std::cout << "Received request " << request->get() << std::endl;

    // Reply a string to the requester.
    request->reply("Done");
}
catch (const coms::InitException& e) {
    std::cout << "Responder error" << std::endl;
}
```
The responder is created with the name "the-responder" to identify it.
The pre-condition for the creation of the responder is that *This* must have been initialized.
Like the return value, it is possible to get the request as **binary** data using the *get()* function or method. It is also possible to reply binary data using the *reply()* method or function.
The requests can also be a **two-part message** and the second part can be get with the *getSecondPart()* method or function.

Let's see an example of requester in Java:
```java
try {
    // Connect to the app RespApp which hosts a responder.
    App responderApp = server.connect("RespApp");

    // Create a requester to the responder "the-responder".
    Requester requester = Requester.create(responderApp, "the-responder");

    // Initialize the requester.
    requester.init();

    System.out.println("Created requester " + requester);

    for (int i = 0; i < N; ++i) {
        // Send a simple message as string.
        requester.send("Message-" + i);

        // Print the response.
        System.out.println("Response is " + requester.receiveString());
    }
				
    // Terminate the requester.
    requester.terminate();		
}
catch (InitException e) {
    System.out.println("Requester error:" + e);
}
```

The requester is created by connecting the responder named "the-responder" living in the *RespApp* application. Once connected, the requester can send requests and receive the responses. Here again the pre-condition for the creation of the requester is that *This* must have been initialized.

The same requester in Python:
```python
try:
    responderApp = server.connect("RespApp")

    requester = cameopy.coms.Requester.create(responderApp, "the-responder")
    requester.init()

    print("Created requester", requester)

    for i in range(N):
        request = "Message-" + str(i)
        requester.send(request)
        response = requester.receiveString()
        print("Response is", response)

    requester.terminate()

except cameopy.InitException:
    print("Requester error")
```

You can notice that no port was provided to define the requester and responder. Only a name was provided. The **ports** are **assigned dynamically** internally but the programmer does not have to care about.

Be careful, the responder must receive requests in a loop after *init()* otherwise any requester will block when initializing.

Notice that the responder can reply **multiple times** for the same request. The requester must then have as many receive() calls as the number of replies.

Notice that we presented the basic responder which cannot process requests in parallel. A **single thread** must be used to receive and reply. If you need to process the requests in parallel, then you have to create some multiple responders.

### Multiple responders

To process the requests in parallel, a set of multi responders must be created. They are attached to a responder router. 

An example of multiple responders in a Java application:

```java
try {
    // Create the router.
    ResponderRouter router = ResponderRouter.create("the-responder");
    router.init();
    
    // Create 5 multi responders.
    for (int i = 0; i < 5; ++i) {
        Thread thread = new Thread(new Runnable() {
            public void run() {
            
                Responder responder = null;
	    		
                try {
                    // Create the multi responder by associating it to the router.
                    responder = Responder.create(router);
                    responder.init();

                    // Receive requests.
                    while (!responder.isCanceled()) {
                        Request request = responder.receive();
                        System.out.println("Received request " + request.getString());

                        // Reply.
                        request.replyString("Response");
                    }

                    responder.terminate();
                }
                catch (InitException e) {
                }	
            }
        });

        thread.start();
    }

    // Start the router by a blocking call.
    router.run();

    // Terminate the router once finished.
    router.terminate();
}
catch (InitException e) {
}

```
Once the router is receiving a request from a requester, it forwards it to a multi responder (round-robin distribution) that can process it in its **own thread**. It can also reply multiple times. The router run() call is **blocking** and the router must be canceled to return.


## Publisher/Subscriber pattern

Another useful communication pattern is the publish/subscribe pattern. It allows asynchronous messages from one application to other ones. Let's define a Java application *PubJava* which defines a publisher:
```java
try {
    // Create the publisher with name "the-publisher".
    Publisher publisher = Publisher.create("the-publisher");

    // Set the number of subscribers to wait to 1.
    publisher.setWaitForSubscribers(1);

    // Synchronize with the subscriber(s). Wait for the subscriber to connect.
    publisher.init();

    // Send data.
    for (int i = 0; i < 100; ++i) {
        publisher.send("message " + i);
    }

    // Terminate the publisher.
    publisher.terminate();
}
catch (InitException e) {
    System.out.println("Publisher error:" + e);
}
```

Here again, *This* must have been initialized before creating the publisher. We provide a synchronization feature with the second argument of the *Publisher.create()* method: the number of subscribers can be provided. The call to *Publisher.init()* is blocking until the required number of subscribers has been reached. This allows to have subscribers that will receive all the messages: The publisher is **synchronized**.
Default value is 0 and in that case *Publisher.init()* immediately returns. Then the publisher is **not** synchronized and some messages may be lost. There is another option for the publisher: synchronized subscribers. It is set using *setSyncSubscribers()* instead of *setWaitForSubscribers()* so that the publisher is not waiting for the subscribers but the subscribers are effectively connected at the end of their *init()*. The implementation requires an additional thread that is why it is not set by default.

Let's give an example of a subscriber in C++:
```cpp
try {
    // Connect to the app PubApp which hosts a publisher.
    std::unique_ptr<App> publisherApp = server.connect("PubApp");

    // Create a subscriber to the application.
    std::unique_ptr<coms::Subscriber> subscriber = coms::Subscriber::create(*publisherApp, "the-publisher");

    // Initialize the subscriber.
    subscriber->init();

    // Receive data.
    while (true) {
        std::optional<std::string> message = subscriber->receive();
    
        // If there is no value then the subscriber will not receive messages any more.
        if (!message.has_value()) {
            break;
        }
        std::cout << "Received " << message.value() << std::endl;
    }
}
catch (const InitException& e) {
    std::cout << "Subscriber cannot be created" << std::endl;
}
```
The subscriber is created by connecting to the publisher named "the-publisher" living in the *PubApp* application. Once connected, the subscriber will receive the messages until an empty message arrives. Here again the pre-condition for the creation of the subscriber is that *This* must have been initialized.

The same subscriber in Python:
```python
try:
    # Connect to the app PubApp which hosts a publisher.
    publisherApp = server.connect("PubApp")

    # Create a subscriber to the application.
    subscriber = cameopy.coms.Subscriber.create(publisherApp, "the-publisher")

    # Initialize the subscriber.
    subscriber.init()

    # Receive data.
    while True:
        message = subscriber.receiveString()
        if message:
            print("Received", message)
        else:
            break

except cameopy.InitException:
    print("Subscriber error")
```

As for the requester/responder no port was provided to define the publisher and subscriber. but only a name. Moreover the CAMEO implementation provides a bit of **synchronization** with the number of subscribers which can be very useful.

The communication patterns provided by CAMEO are **high-level** and allow programmers to rapidly design a communication between apps.
