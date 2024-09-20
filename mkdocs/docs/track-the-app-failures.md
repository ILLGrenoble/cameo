# Track the app failures


In CAMEO, an app that crashes is not an unexpected behavior but is a common use case. However how to track the failures? How to interrupt a communication with an app responder or publisher if the app has crashed?

## Control the state
We already saw the different ways of getting the state of an app in a previous section.
To track the failure it is possible to poll using *getLastState()*. In Java:

```java
// Get an app.
App app = server.connect("App2");

// Infinite loop.
while (true) {
    if (app.getLastState() == State.FAILURE) {
        System.out.println("Application " + app.getName() + " failed!");
        break;
    }
    else if (app.getLastState() == State.SUCCESS) {
        System.out.println("Application " + app.getName() + " finished successfully");
        break;
    }
    else if (app.getLastState() == State.STOPPED || app.getLastState() == State.KILLED) {
        System.out.println("Application " + app.getName() + " has been terminated");
        break;
    }
}
```
Using *getLastState()*, the app can have crashed a long time ago but the *app* object will still return the last state of the running program.

You can also wait for the termination of the app and in that case you will get the terminal state. In Python:
```python
# Get an app.
app = server.connect("App2")

# Wait for its termination.
state = app.waitFor()

# Check the state.
if state == cameopy.FAILURE:
    print("Application", app.getName(), "terminated with failure")
```

Using *waitFor()* you will get the notification of the failure as soon as it happens however it is a blocking call so that you will have to put it in a thread to control the code.
That is the strategy of the *check app* feature of the coms classes shown in the next section.

## Failure of a Responder app

Suppose you defined a *Requester* and connected it to a *Responder* defined on a remote app. What happens if the remote app is crashing?
A timeout can be set for a *Requester* so that the *receive()* call will return if the remote app has crashed and is unable to respond. However it is possible to not set a timeout and the *Requester* shall wait indefinitely in the *receive()* call. To avoid the blocking, you can define the *Requester* with the *check app* feature enabled. In Java:

```java
// Connect to the app RespApp which hosts a responder.
App responderApp = server.connect("RespApp");

// Create a requester to the responder "the-responder" with the check app feature enabled.
Requester requester = Requester.create(responderApp, "the-responder");
requester.setCheckApp(true);

// Initialize the requester.
requester.init();

// Send requests.
while (true) {
    // Send a simple message as string.
    requester.send("Message");

    // Print the response.
    byte[] response = requester.receive();
    
    // Check the response.
    if (response == null) {
        int lastState = responderApp.getLastState();
        if (lastState == State.FAILURE) {
            System.out.println("Responder app terminated with state FAILURE");
            break;
        }
    }
}

// Terminate the requester.
requester.terminate();		
```
We check the response from *receive()* which is null only if the *Requester* is canceled.
If the *Responder* app is crashing then the *Requester* is automatically canceled and the *receive()* call returns. Notice that the *check app* feature requires an additional thread for the *Requester*.

## Failure of a Publisher app

Suppose you defined a *Subscriber* and connected it to a *Publisher* defined on a remote app. What happens if the remote app is crashing?
If the remote app has crashed the *Subscriber* will wait indefinitely in the *receive()* call. To avoid the blocking, you can define the *Subscriber* with the *check app* feature enabled.
In C++:
```c++
// Connect the app PubApp.
std::unique_ptr<App> publisherApp = server->connect("PubApp");

// Create a subscriber with the feature check app enabled.
std::unique_ptr<coms::Subscriber> subscriber = coms::Subscriber::create(*publisherApp, "publisher");
subscriber->setCheckApp(true);

// Initialize the subscriber.
subscriber->init();

// Receive data.
while (true) {
	std::optional<string> data = subscriber->receive();

	// Check the data, exit the loop if there is no data.
	if (!data.has_value()) {
		break;
	}
}

// The publication is finished.
State state = publisherApp->waitFor();
if (state == FAILURE) {
	std::cout << "Publisher application terminated with state FAILURE" << std::endl;
}
```

We check the response from *receive()* which is null only if the publication is terminated.
If the *Publisher* app has crashed then the *receive()* call returns and we can check the state of the app.
