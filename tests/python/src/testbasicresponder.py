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

server = cameopy.Server(endpoint, 0, useProxy)

print("Responder application is", applicationName)

for i in range(numberOfTimes):
    
    args = ["true" if useProxy else "false"]
    app = server.start(applicationName, args)
    print("Started application", applicationName)

    requester = cameopy.Requester.create(app, "responder")
    
    # Send a simple message.
    requester.send("request")
    
    response = requester.receive()
    print("Response is", response)
    
    # Send a two-parts message.
    requester.sendTwoBinaryParts("first", "second")
    
    response = requester.receive()
    print("Response is", response)
    
    # Send a simple message but do not receive the response immediately.
    requester.send("request");
    
    print("Wait so that the responder has timed out")
    time.sleep(1)
    
    response = requester.receive()
    print("Response is", response)
    
    # Send a new simple message.
    requester.send("request after timeout")

    response = requester.receive()
    print("Response is", response)
    
    # Wait for the application.
    app.waitFor()
    
    print("Finished the application")


    