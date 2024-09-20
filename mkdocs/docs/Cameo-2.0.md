Changes:
* Messaging architecture reviewed to support proxies for responders and publishers.
* By default started applications are linked to the starter app i.e. if the starter has stopped, the application is also stopped.
* Added interfaces Object, Timeoutable, Cancelable in C++ and Java.
* APIs review
* Use of *init()* for Server and coms classes. The class *InitException* replaces creation exceptions.
* Removed *exists()*  and throw an exception *StartException* if the application does not exist.
* Requester/Responder basic reimplementation.
* Requester/Responder multi reimplementation.
* Publisher/Subscriber reimplementation.
* New port strategy for coms classes: a port becomes unavailable if it cannot be assigned.
* One ZeroMQ context shared between all the servers.
* Renamings:
  - Removed *application* namespace in C++ and *Application* class in Java
  - Class *Instance* becomes *App*
  - Class *Configuration* becomes *Config* in *App*
  - Class *Info* becomes *Info* in *App*
  - Class *Port* becomes *Port* in *App*
  - *This::cancelWaitings()* becomes *This::cancelAll()*
  - *This::setBinaryResult()* is removed in C++
  - *This::setResult()* becomes *This::setStringResult()* in Java
  - *App::now()* is removed in C++
  - *App::getBinaryResult()* is removed in C++
  - *Request::getBinary()* is removed in C++
  - *Request::replyBinary()* is removed in C++
  - *Request::getSecondBinaryPart()* becomes *Request::getSecondPart()* in C++
  - *Request.getBinary()* becomes *Request.get()* in Java
  - *Request.get()* becomes *Request.getString()* in Java
  - *Request.getBinary()* becomes *Request.get()* in Java
  - *Request.getTwoBinaryParts()* becomes *Request.getTwoParts()* in Java
  - *Request.reply()* becomes *Request.replyString()* in Java
  - *Requester::sendBinary()* is removed in C++
  - *Requester.send()* becomes *sendString()* in Java
  - *Requester::sendTwoBinaryParts()* becomes *Requester::sendTwoParts()* in C++
  - *Requester::receiveBinary()* is removed in C++
  - *Requester.send()* becomes *Requester.sendString()* in Java
  - *Publisher::sendBinary()* is removed in C++
  - *Publisher::sendTwoBinaryParts()* becomes *Publisher::sendTwoParts()* in C++
  - *Publisher.send()* becomes *Publisher.sendString()* in Java
  - *Subscriber::receiveBinary()* is removed in C++
  - *Subscriber::receiveTwoBinaryParts()* becomes *Subscriber::receiveTwoParts()* in C++