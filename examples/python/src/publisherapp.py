#!/usr/bin/python3 -u
import sys
import cameopy
import time

this = cameopy.This
this.init(sys.argv)

numberOfSubscribers = 0 if len(sys.argv) < 3 else int(sys.argv[1])

publisher = cameopy.Publisher.create("the-publisher", numberOfSubscribers)
publisher.init()
print("Created publisher", publisher.getName())

publisher.waitForSubscribers()
print("Synchronized with", numberOfSubscribers, "subscriber(s)")

this.setRunning()

i = 0
while True:
  message = "a message " + str(i)  
  publisher.send(message)
  
  i += 1
  
  time.sleep(1.000)
  
