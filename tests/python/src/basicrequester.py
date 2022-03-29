import sys
import time
import cameopy

this = cameopy.This
this.init(sys.argv)

useProxy = False if len(sys.argv) < 2 else (sys.argv[1] == "true")

starter = this.connectToStarter(0, useProxy)

app = starter.getApp()

# Create a requester.
requester = cameopy.Requester.create(app, "responder")

this.setRunning()

# Send 10 requests.
R = 10
for i in range(R):
    # Send and wait for the result.
    requester.send("test")

    response = requester.receive()

    if response:
        print("Received",  response)

    time.sleep(0.1)

print("Finished the application")
