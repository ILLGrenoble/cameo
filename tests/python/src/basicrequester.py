import sys
import time
import cameopy

this = cameopy.This
this.init(sys.argv)

useProxy = False if len(sys.argv) < 2 else (sys.argv[1] == "true")

options = 0
if useProxy:
    options |= cameopy.USE_PROXY

starter = this.connectToStarter(options)

app = starter.getApp()

# Create a requester.
requester = cameopy.coms.Requester.create(app, "responder")
requester.init()

this.setRunning()

# Send 10 requests.
R = 10
for i in range(R):
    # Send and wait for the result.
    requester.send("test")

    response = requester.receiveString()

    if response:
        print("Received",  response)

    time.sleep(0.1)

starter.terminate()

print("Finished the application")
