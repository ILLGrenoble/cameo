import sys
import time
import cameopy

this = cameopy.This
this.init(sys.argv)

publisher = cameopy.Publisher.create("publisher", 1)
publisher.init()
publisher.waitForSubscribers()

this.setRunning()

print("Synchronized with the subscriber")

while (True):
    publisher.send("hello")
    time.sleep(0.1)

print("Finished the application")
