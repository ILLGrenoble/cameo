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

this.heartbeat(1, 1)
    
args = ["true" if useProxy else "false"]
heartbeatApp = server.start(applicationName, args)

requester = cameopy.coms.Requester.create(heartbeatApp, "responder")
requester.init()

print("Requester ready ?", requester.isReady())

publisher = cameopy.coms.Publisher.create("publisher")
publisher.init()

print("Publisher ready ?", publisher.isReady())

N = 5
for i in range(N):
    print(i + 1, "/", N)
    time.sleep(1)

print("Waiting for the application")

heartbeatApp.waitFor()

requester.terminate()
publisher.terminate()
   
print("Finished the application")

server.terminate()
this.terminate()
    