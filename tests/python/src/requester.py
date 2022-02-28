import sys
import time
import cameopy

this = cameopy.This
this.init(sys.argv)

starter = this.connectToStarter()

app = starter.getInstance()

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
