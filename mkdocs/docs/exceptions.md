# Exceptions

A summary of the exceptions that can be thrown in a program using the CAMEO API:

## List of exceptions

**Exception**                    | **Where**               | **Description**
---------------------------------|-------------------------|----------------
ConnectionTimeout                | Server.init(), Requester.init(), Subscriber.init(), all functions or methods accessing a remote Server           | A timeout occurs in a connection.
InitException                    | Server.init(), Responder.init(), ResponderRouter.init(), Requester.init(), Publisher.init(), Subscriber.init()      | Cannot initialize the object.
SynchronizationTimeout           | Requester.init(), Subscriber.init() | Timeout occurs during the synchronization of objects.
InvalidArgumentException         | Server.init()           | Bad endpoint value.
StartException                   | Server.start()          | The application cannot be started.
UnregisteredApplicationException | This.init()             | Maximum number of running apps reached.
KeyAlreadyExistsException        | This.Com.storeKeyValue()| Key is already stored for the application.
UndefinedKeyException            | This.Com.getKeyValue(), App.Com.getKeyValue() | Key does not exist.

## More details on the exceptions

The most common exception is *ConnectionTimeout* that can occur in any call to a remote *Server* object.
As the process of the requests to a remote *Server* object should be quick, a *ConnectionTimeout* should mean that the network was not fast enough or the remote computer is not accessible any more.

However a call to a local *Server* object may not throw such a *ConnectionTimeout* exception. That is why a good practice is:

- Set a timeout to remote *Server* objects.
- Do not set a timeout to local *Server* objects.

A *SynchronizationTimeout* exception has a different meaning. It concerns blocking *init()* calls for the *Requester* and *Subscriber* objects. Indeed, a *Requester* may block indefinitely if the *Responder* is never ready. In that case, setting a timeout makes the *init()* call exit.

An *InitException* exception should be thrown if the underlying communication object cannot be initialized i.e. ZeroMQ sockets.

An *InvalidArgumentException* happens when the provided endpoint is invalid e.g. "tc://serv:10000" or "tcp://serv:".

The *StartException* exception occurs when the *CAMEO* server is unable to start the requested application. It can happen if the program is not accessible or the maximum number of running apps is reached.

The *UnregisteredApplicationException* exception occurs for an application that is not registered in the CAMEO server and tries to attach to it. If the maximum number of running apps is reached, it cannot attach.

The exceptions *KeyAlreadyExistsException* and *UndefinedKeyException* are thrown in the communication objects. These exceptions concern the implementation of a communication object e.g. *Requester*, *Responder*, etc.

Notice that a *Requester.receive()* call does not throw an exception if a timeout occurs. However the object returned is null and *Requester.hasTimedout()* returns true.

