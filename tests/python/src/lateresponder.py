import sys
import time
import cameopy

this = cameopy.This
this.init(sys.argv)

useProxy = False if len(sys.argv) < 2 else (sys.argv[1] == "true")

options = 0
if useProxy:
    options |= cameopy.option.USE_PROXY

print("Creating responder after 1s")

time.sleep(1)

responder = cameopy.coms.basic.Responder.create("responder")
responder.init()

this.setRunning()

# Receive first request.
request = responder.receive();
print("Received request", request.getString())

request.reply("The response")

responder.terminate()
this.terminate()

print("Finished the application")
