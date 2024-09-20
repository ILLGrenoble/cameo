import sys
import time
import threading
import cameopy

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

def subscriberThread(server):
    thisApp = server.connect(this.getName())
    
    subscriber = cameopy.coms.Subscriber.create(thisApp, "publisher")
    subscriber.init()
    
    print("Created subscriber")
    
    data = subscriber.receiveString()
    if data:
        print("Received", data)

    subscriber.setTimeout(500)
    
    data = subscriber.receiveString()
    if data:
        print("Received", data)
    else:
        print("Has not received data, has timedout", subscriber.hasTimedout())

    data = subscriber.receiveString()
    if data:
        print("Received", data)
    else:
        print("Has not received data, has timedout", subscriber.hasTimedout())

t = threading.Thread(target=subscriberThread, args=(server,))
t.start()

publisher = cameopy.coms.Publisher.create("publisher")
publisher.setSyncSubscribers(True)
publisher.setWaitForSubscribers(1)
publisher.init()

publisher.send("first message")

time.sleep(1)

publisher.send("message after timeout")

t.join()

server.terminate()

print("Finished the application")
