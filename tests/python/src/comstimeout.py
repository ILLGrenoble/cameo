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

print("Connected to starter")

app = starter.getApp()

# Create a requester.
requester = cameopy.coms.Requester.create(app, "an unknown responder")
requester.setTimeout(500)

this.setRunning()

try:
    requester.init()
except cameopy.InitException as e:
    print(e)

requester.terminate()


# Create a subscriber.
subscriber = cameopy.coms.Subscriber.create(app, "an unknown publisher")
subscriber.setTimeout(500)

try:
    subscriber.init()
except cameopy.InitException as e:
    print(e)

subscriber.terminate()


# Create a subscriber.
subscriber = cameopy.coms.Subscriber.create(app, "pub")
subscriber.setTimeout(500)

try:
    subscriber.init()
except cameopy.InitException as e:
    print(e)

subscriber.terminate()


starter.terminate()

print("Finished the application")
