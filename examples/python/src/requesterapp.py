#!/usr/bin/python3 -u

import sys
import cameopy

numberOfTimes = 1 if len(sys.argv) < 3 else int(sys.argv[1])
print("Number of times", numberOfTimes)

this = cameopy.This
this.init(sys.argv)

server = this.getServer()
responderApp = server.connect("responderpy", 0)

print("Connected", responderApp)

requester = cameopy.Requester.create(responderApp, "the-responder")
requester.init()

for i in range(numberOfTimes):
  request = "request-" + str(i)
  requester.send(request)
  print("Sent request")
  response = requester.receiveString()
  print("Response", response)
