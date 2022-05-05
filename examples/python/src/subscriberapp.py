#!/usr/bin/python3 -u

import sys
import cameopy

this = cameopy.This
this.init(sys.argv)

server = this.getServer()
publisherApp = server.connect("publisherpy", 0)

print("Connected", publisherApp)

subscriber = cameopy.coms.Subscriber.create(publisherApp, "the-publisher")
subscriber.init()

print("Created subscriber", subscriber)

while True:
  message = subscriber.receiveString()
  if message:
    print("Received", message)
  else:
    print("Canceled")
