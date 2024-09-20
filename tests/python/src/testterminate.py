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

options = 0
endpoint = "tcp://localhost:11000";
if useProxy:
    options |= cameopy.USE_PROXY
    endpoint = "tcp://localhost:12000";

server = cameopy.Server.create(endpoint, options)
server.init()

thisApp = server.connect(cameopy.This.getName())

print("Creating basic responder and requester")
    
responder = cameopy.coms.basic.Responder.create("responder")
responder.init()

# Start reponder thread.
tr = threading.Thread(target=loopResponder, args=(responder,))
tr.start()
    
requester = cameopy.coms.Requester.create(thisApp, "responder")
requester.init()
    
# Start cancel thread.
tc = threading.Thread(target=cancelRequester, args=(requester, responder,))
tc.start()

tr.join()
tc.join()

print("Creating publisher and subscriber")

publisher = cameopy.coms.Publisher.create("publisher")
publisher.init()

subscriber = cameopy.coms.Subscriber.create(thisApp, "publisher")
subscriber.init()

requester.terminate()
responder.terminate()
publisher.terminate()
subscriber.terminate()
thisApp.terminate()
server.terminate()
this.terminate()

print("Finished the application")
