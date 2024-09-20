import sys
import cameopy

this = cameopy.This
this.init(sys.argv)

useProxy = False if len(sys.argv) < 2 else (sys.argv[1] == "true")

print("Creating responder")

responder = cameopy.coms.basic.Responder.create("responder")
responder.init()

this.setRunning()

# Receive first request.
request = responder.receive();
print("Received request", request.getString())

request.reply("response")

print("Finished the application")

sys.exit(123)