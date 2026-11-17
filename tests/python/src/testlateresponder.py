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

options = 0
endpoint = "tcp://localhost:11000";
if useProxy:
    options |= cameopy.option.USE_PROXY
    endpoint = "tcp://localhost:12000";

server = cameopy.Server.create(endpoint, options)
server.init()

print("Responder application is", applicationName)

for i in range(numberOfTimes):
    
    args = ["true" if useProxy else "false"]
    app = server.start(applicationName, args)
    print("Started application", applicationName)

    requester = cameopy.coms.Requester.create(app, "responder")
    requester.setCheckApp(True)
    requester.init()
    
    print("Created requester", requester)
        
    # Send a simple message.
    response = requester.requestString("request")
    print("Response is", response)
        
    # Wait for the application.
    app.waitFor()
    
    requester.terminate()
    
    print("Finished the application")

server.terminate()
this.terminate()
    