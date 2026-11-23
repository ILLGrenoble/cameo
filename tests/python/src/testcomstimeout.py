import sys
import time
import threading
import cameopy

this = cameopy.This
this.init(sys.argv)

useProxy = False if len(sys.argv) < 3 else (sys.argv[1] == "true") 

endpoint = "tcp://localhost:11000";
if useProxy:
    endpoint = "tcp://localhost:11000:12000";

server = cameopy.Server.create(endpoint)
server.init()

this.setRunning()

app = server.start("comstimeoutpy")
app.waitFor(cameopy.state.RUNNING)

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

server.terminate()
this.terminate()

print("Finished the application")

