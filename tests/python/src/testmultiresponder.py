import sys
import time
import cameopy

this = cameopy.This
this.init(sys.argv)

applicationName = ""
numberOfTimes = 1 

if len(sys.argv) >= 3:
    applicationName = sys.argv[1]
    
if len(sys.argv) >= 4:
    numberOfTimes = int(sys.argv[2])

useProxy = False if len(sys.argv) < 5 else (sys.argv[3] == "true") 

endpoint = "tcp://localhost:11000";
if useProxy:
    endpoint = "tcp://localhost:10000";

server = cameopy.Server.create(endpoint, 0, useProxy)
server.init()

for i in range(numberOfTimes):
    
    app = server.start(applicationName)
    print("Started application", applicationName)

    requester = cameopy.Requester.create(app, "responder")
    
    # Send a simple message.
    requester.send("request")
    
    response = requester.receiveString()
    print("Response is", response)
    
    # Send a two-parts message.
    requester.sendTwoParts("first", "second")
    
    response = requester.receiveString()
    print("Response is", response)
        
    # Wait for the application.
    app.waitFor()
    
    print("Finished the application")


    