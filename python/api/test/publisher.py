#!/usr/bin/python3
import sys

#sys.path.append('/usr/local/src/cameo-api-cpp/cameo-python/build/cameopython-prefix/src/cameopython-build/')

import cameopy
t= cameopy.This

t.init( sys.argv)
print("Name: ", t.getName())
print("Id: ", t.getId())
print("Timeout: ", t.getTimeout())
t.setRunning()


publ = cameopy.Publisher.create("publisherpython",1)
print("Publisher name: ",publ.getName())
print("Publisher: ", str(publ))

starter = t.connectToStarter()

publ.waitForSubscribers()
publ.send("this is a text message")
publ.send("another message")
publ.sendEnd()
print(publisher.isEnded())
