#!/usr/bin/python3 -u

import sys
import cameopy

this = cameopy.This
this.init(sys.argv)

responder = cameopy.Responder.create("the-responder")
print("Created responder", responder.getName())

this.setRunning()

while True:
  request = responder.receiveString();
  if request:
    requestStr = request.getString()
    print("received request", requestStr)
    response = "processed " + requestStr
    request.reply(response)
  else:
    print("canceled")  
  
