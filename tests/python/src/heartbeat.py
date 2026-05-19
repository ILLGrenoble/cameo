import sys
import time
import threading
import cameopy

def cancelResponder(responder):
    time.sleep(2)
    responder.cancel()

this = cameopy.This
this.init(sys.argv)

useProxy = False if len(sys.argv) < 2 else (sys.argv[1] == "true")

options = 0
if useProxy:
    options |= cameopy.option.USE_PROXY

print("Creating responder")

responder = cameopy.coms.basic.Responder.create("responder")
responder.init()

print("Created responder")

this.setRunning()

# Create the cancel thread
t = threading.Thread(target=cancelResponder, args=(responder,))
t.start()

responder.receive()
    
t.join()

responder.terminate()
this.terminate()

print("Finished the application")
