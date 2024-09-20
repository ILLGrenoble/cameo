import sys
import time
import cameopy

this = cameopy.This
this.init(sys.argv)

useProxy = False if len(sys.argv) < 2 else (sys.argv[1] == "true")

options = 0
if useProxy:
    options |= cameopy.option.USE_PROXY

print("Creating responder")

responder = cameopy.coms.basic.Responder.create("responder")
responder.init()

this.setRunning()

# Receive first request.
request = responder.receive();
print("Received request", request.getString())

request.reply("1st response")
request.reply("1st response (bis)")

# Receive second request.
request = responder.receive();

sp = request.getSecondPart()

print("Received request with parts", request.getString(), request.getSecondPart().decode("utf-8"))

request.reply("2nd response")

#if not res:
#    print("Error, no timeout expected with", request)    


# Receive third request.
request = responder.receive();
print("Received request", request.getString())

request.reply("3rd response")

# Receive the fourth request.
request = responder.receive();
print("Received request", request.getString())

print("Wait so that the requester has timed out")

time.sleep(1)

request.reply("4th response")

# Receive the fifth request.
request = responder.receive();
print("Received request " + request.getString())

# Reply.
request.reply("5th response")

print("Replied 5th")

# Test connection.
requester = request.connectToRequester(options)
app = requester.getApp()

print("Requester", app.getId())

print("Finished the application")
