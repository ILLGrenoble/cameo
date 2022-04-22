import sys
import cameopy

this = cameopy.This
this.init(sys.argv)

numberOfSubscribers = 1 if len(sys.argv) < 3 else int(sys.argv[1])
print("Number of subscribers", numberOfSubscribers)

print("Creating publisher and waiting for ", numberOfSubscribers, "subscriber(s)...")

publisher = cameopy.Publisher.create("publisher", numberOfSubscribers)
publisher.init()

this.setRunning()

print("Synchronized with ", numberOfSubscribers, "subscriber(s)")

for i in range(100):
    message = "message " + str(i)  
    publisher.send(message)

publisher.sendEnd()

print("Finished the application")
