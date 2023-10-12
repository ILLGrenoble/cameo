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
    options |= cameopy.USE_PROXY
    endpoint = "tcp://localhost:10000";

server = cameopy.Server.create(endpoint, options)
server.init()

this.setRunning()

app = server.start("comstimeoutpy")
app.waitFor(cameopy.RUNNING)

publisher = cameopy.coms.Publisher.create("pub")
publisher.setWaitForSubscribers(2)

def initPublisher(publisher):
    publisher.init()


t = threading.Thread(target=initPublisher, args=(publisher,))
t.start()

time.sleep(200/1000)

publisher.cancel()
t.join()

print("Canceled publisher")

app.waitFor()

print("Finished the application")

