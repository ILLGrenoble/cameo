import sys
import cameopy

this = cameopy.This
this.init(sys.argv)

print("Creating responder")

responder = cameopy.Responder.create("responder")

this.setRunning()

# Receive first request.
request = responder.receive();
print("Received request", request.get())

request.reply("1st response")

# Receive second request.
request = responder.receive();

print("Received request with parts", request.get(), request.getSecondBinaryPart())

request.reply("2nd response")

# Receive third request without receive on the requester side.
request = responder.receive();
print("Received request", request.get())

# Reply with timeout.
request.setTimeout(100)

res = request.reply("3rd response")

if not res:
    print("Timeout with", request.getObjectId())    

# Receive request after timeout.
request = responder.receive();
print("Received request", request.get())

request.reply("4th response after timeout")

print("Finished the application")
