import sys
import cameopy
import time
import json

# Initialize cameo.
this = cameopy.This
this.init(sys.argv)

# Define the stop handler to properly stop.
def stop():
    print("Stop the app")

this.handleStop(stop)

numberOfSubscribers = 1

# Create the publisher.
publisher = cameopy.coms.Publisher.create("the-publisher")
publisher.setWaitForSubscribers(numberOfSubscribers)

print("Created publisher", publisher.getName())

publisher.init()

print("Synchronized with", numberOfSubscribers, "subscriber(s)")

# Set the state.
this.setRunning()

i = 0
while not this.isStopping():
    # Send a message.
    message = json.dumps({'message': "a message", 'value': i})
    publisher.send(message)
  
    i += 1
  
    time.sleep(1.000)

# Terminate the publisher and This.
publisher.terminate()
this.terminate()