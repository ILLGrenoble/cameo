import sys
import time
import threading
import cameopy

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

endpoint = "tcp://localhost:11000";
if useProxy:
    endpoint = "tcp://localhost:10000";

server = cameopy.Server.create(endpoint, 0, useProxy)
server.init()

thisApp = server.connect(cameopy.This.getName())

print("Creating basic responder and requester")
    
responder = cameopy.BasicResponder.create("responder")

# Start reponder thread.
tr = threading.Thread(target=loopResponder, args=(responder,))
tr.start()
    
requester = cameopy.Requester.create(thisApp, "responder")
    
# Start cancel thread.
tc = threading.Thread(target=cancelRequester, args=(requester, responder,))
tc.start()

tr.join()
tc.join()

print("Creating publisher and subscriber")

publisher = cameopy.Publisher.create("publisher")
subscriber = cameopy.Subscriber.create(thisApp, "publisher")

requester.terminate()
responder.terminate()
publisher.terminate()
subscriber.terminate()
thisApp.terminate()
server.terminate()
this.terminate()

print("Finished the application")
