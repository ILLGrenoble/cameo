import sys
import time
import threading
import cameopy

def cancelAll():
    time.sleep(1)
    cameopy.This.cancelAll()

def cancelWaitFor(instance):
    time.sleep(1)
    instance.cancel()

def cancelWaitForSubscribers(publisher):
    time.sleep(1)
    publisher.cancel()

def killApplication(app):
    time.sleep(1)
    app.kill()

def cancelResponder(responder):
    time.sleep(1)
    responder.cancel()

def loopResponder(responder):
    
    while not responder.isCanceled():
        responder.receive()
    

def cancelRequester(requester, responder):
    time.sleep(1)
    requester.cancel()
    responder.cancel()


this = cameopy.This
this.init(sys.argv)

useProxy = False if len(sys.argv) < 3 else (sys.argv[1] == "true") 

options = 0
endpoint = "tcp://localhost:11000";
if useProxy:
    options |= cameopy.option.USE_PROXY
    endpoint = "tcp://localhost:12000";

server = cameopy.Server.create(endpoint, options)
server.init()

def testCancelAll():

    print("Starting stopcpp for cancelAll")
    
    app = server.start("stopcpp")
    
    # Start thread.
    t = threading.Thread(target=cancelAll)
    t.start()
    
    app.waitFor()
    
    print("End of waitFor")
    
    app.stop()
    state = app.waitFor()
    
    t.join()
    
    print("End of stopcpp with state", cameopy.toString(state))
    
    app.terminate()


def testCancelWaitFor():

    print("Starting stopcpp for cancelWaitFor")
    
    app = server.start("stopcpp")
    
    # Start thread.
    t = threading.Thread(target=cancelWaitFor, args=(app,))
    t.start()
    
    app.waitFor()
    
    print("End of waitFor")
    
    app.stop()
    state = app.waitFor()
    
    t.join()
    
    print("End of stopcpp with state", cameopy.toString(state))
    
    app.terminate()

    
def testCancelWaitForSubscribers():

    print("Creating publisher and waiting for 1 subscriber...")

    publisher = cameopy.coms.Publisher.create("publisher")
    publisher.setWaitForSubscribers(1)

    # Start thread.
    t = threading.Thread(target=cancelWaitForSubscribers, args=(publisher,))
    t.start()

    publisher.init()

    t.join()

    print("Synchronization with the subscriber", not publisher.isCanceled())
    
    publisher.terminate()


def testKillApplication():
        
    print("Starting publisherlooppy for killing")

    app = server.start("publisherlooppy")

    # Start kill thread.
    t = threading.Thread(target=killApplication, args=(app,))
    t.start()

    subscriber = cameopy.coms.Subscriber.create(app, "publisher")
    subscriber.setCheckApp(True)
    subscriber.init()

    # Receiving data.
    while True:
        data = subscriber.receiveString()
        if data:
            print("Received", data)
        else:
            break

    print("Subscriber end of stream", subscriber.hasEnded())

    state = app.waitFor()

    print("End of publisherlooppy with state", cameopy.toString(state))

    t.join()
    
    subscriber.terminate()
    app.terminate()
    

def testCancelSubscriber():
    
    print("Starting publisherlooppy for testing cancel of a subscriber")

    app = server.start("publisherlooppy")

    # Create a subscriber.
    subscriber = cameopy.coms.Subscriber.create(app, "publisher")
    subscriber.setCheckApp(True)
    subscriber.init()
    
    # Start cancel thread.
    t = threading.Thread(target=cancelAll)
    t.start()

     # Receiving data.
    while True:
        data = subscriber.receiveString()
        if data:
            print("Received", data)
        else:
            break

    print("Subscriber end of stream", subscriber.hasEnded())
    
     # Start kill thread.
    k = threading.Thread(target=killApplication, args=(app,))
    k.start()

    state = app.waitFor()

    print("End of publisherloopcpp with state", cameopy.toString(state))

    t.join()
    k.join()
    
    subscriber.terminate()
    app.terminate()


def testResponder():
    
    print("Creating basic responder and waiting for requests")
    
    responder = cameopy.coms.basic.Responder.create("responder")
    responder.init()
    
    # Start cancel thread.
    t = threading.Thread(target=cancelResponder, args=(responder,))
    t.start()

    print("Wait for requests")

    request = responder.receive()
    
    if request is not None:
        print("Responder error: receive should return None")

    t.join()
    
    responder.terminate()
    

def testRequester():
    
    print("Creating basic responder and requester")
    
    responder = cameopy.coms.basic.Responder.create("responder")
    responder.init()
    
    # Start reponder thread.
    tr = threading.Thread(target=loopResponder, args=(responder,))
    tr.start()
    
    thisApp = server.connect(cameopy.This.getName())
    
    requester = cameopy.coms.Requester.create(thisApp, "responder")
    requester.init()
    
    # Start cancel thread.
    tc = threading.Thread(target=cancelRequester, args=(requester, responder,))
    tc.start()

    print("Sending request")

    requester.send("request")

    print("Receiving response")
    
    requester.receive()
    
    if requester.isCanceled():
        print("Requester is canceled")
    else:
        print("Requester is not canceled")

    tc.join()
    tr.join()

    requester.terminate()    
    responder.terminate()

    
testCancelAll()
testCancelWaitFor()
testCancelWaitForSubscribers()
testKillApplication()
testCancelSubscriber()
testResponder()
testRequester()

server.terminate()
this.terminate()

print("Finished the application")
