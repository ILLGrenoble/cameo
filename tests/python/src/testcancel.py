import sys
import time
import threading
import cameopy

def cancelAll():
    time.sleep(1)
    cameopy.This.cancelWaitings()

def cancelWaitFor(instance):
    time.sleep(1)
    instance.cancelWaitFor()

def cancelWaitForSubscribers(publisher):
    time.sleep(1)
    publisher.cancelWaitForSubscribers()

def killApplication(app):
    time.sleep(1)
    app.kill()

this = cameopy.This
this.init(sys.argv)
server = this.getServer()

def testCancelAll():

    print("Starting stopcpp for cancelWaitings")
    
    app = server.start("stopcpp")
    
    # Start thread.
    t = threading.Thread(target=cancelAll)
    t.start()
    
    app.waitFor()
    
    print("End of waitFor")
    
    app.stop()
    state = app.waitFor()
    
    t.join()
    
    print("End of stopcpp with state", state)


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
    
    print("End of stopcpp with state", state)

    
def testCancelWaitForSubscribers():

    print("Creating publisher and waiting for 1 subscriber...")

    publisher = cameopy.Publisher.create("publisher", 1)

    # Start thread.
    t = threading.Thread(target=cancelWaitForSubscribers, args=(publisher,))
    t.start()

    synced = publisher.waitForSubscribers()

    t.join()

    print("Synchronization with the subscriber", synced)


def testKillApplication():
        
    print("Starting publisherlooppy for killing")

    app = server.start("publisherlooppy")

    # Start kill thread.
    t = threading.Thread(target=killApplication, args=(app,))
    t.start()

    subscriber = cameopy.Subscriber.create(app, "publisher")

    # Receiving data.
    while True:
        data = subscriber.receive()
        if data:
            print("Received", data)
        else:
            break

    print("Subscriber end of stream", subscriber.isEnded())

    state = app.waitFor()

    print("End of publisherlooppy with state ", state)

    t.join()


def testCancelSubscriber():
    
    print("Starting publisherlooppy for testing cancel of a subscriber")

    app = server.start("publisherlooppy")

    # Create a subscriber.
    subscriber = cameopy.Subscriber.create(app, "publisher")
    
    # Start cancel thread.
    t = threading.Thread(target=cancelAll)
    t.start()

     # Receiving data.
    while True:
        data = subscriber.receive()
        if data:
            print("Received", data)
        else:
            break

    print("Subscriber end of stream", subscriber.isEnded())
    
     # Start kill thread.
    k = threading.Thread(target=killApplication, args=(app,))
    k.start()

    state = app.waitFor()

    print("End of publisherloopcpp with state", cameopy.toString(state))

    t.join()
    k.join()
    

    
testCancelAll()
testCancelWaitFor()
testCancelWaitForSubscribers()
testKillApplication()
testCancelSubscriber()

print("Finished the application")
