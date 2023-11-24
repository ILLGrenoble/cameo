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
    options |= cameopy.USE_PROXY
    endpoint = "tcp://localhost:12000";

server = cameopy.Server.create(endpoint, options)
server.init()

print("Requester application is", applicationName)
print("Creating responder")

responder = cameopy.coms.basic.Responder.create("responder")
responder.init()

print("Created responder", responder)

N = 5

for i in range(numberOfTimes):
    
    apps = []
    
    for j in range(N):
        args = ["true" if useProxy else "false"]
        apps.append(server.start(applicationName, args))
        print("Started application", applicationName)
    
    # Process the requests, the requester application sends 10 requests.
    for j in range(N * 10):
        # Receive the simple request.
        request = responder.receive()
        request.reply("done")

        print("Processed", request)
        
    # Wait for the requester applications.
    for j in range(N):
        apps[j].waitFor()
        print("Finished application", apps[j])

print("Finished the application")


    