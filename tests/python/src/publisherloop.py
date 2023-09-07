import sys
import time
import cameopy

this = cameopy.This
this.init(sys.argv)

publisher = cameopy.coms.Publisher.create("publisher")
publisher.setWaitForSubscribers(1)
publisher.init()

this.setRunning()

print("Synchronized with the subscriber")

while (True):
    publisher.send("hello")
    time.sleep(0.1)

print("Finished the application")
