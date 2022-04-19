import sys
import cameopy

this = cameopy.This
this.init(sys.argv)

numberOfTimes = 1 if len(sys.argv) < 3 else int(sys.argv[1])
useProxy = False if len(sys.argv) < 4 else (sys.argv[2] == "true") 

endpoint = "tcp://localhost:11000";
if useProxy:
    endpoint = "tcp://localhost:10000";

server = cameopy.Server.create(endpoint, 0, useProxy)
server.init()

for i in range(numberOfTimes):
    try:   
        server.start("fuzz")
    except cameopy.AppStartException:
        print("Application fuzz cannot be started")    
    
    try:   
        server.connect("fuzz")    
    except cameopy.AppConnectException:
        print("Application fuzz cannot be connected")
    
