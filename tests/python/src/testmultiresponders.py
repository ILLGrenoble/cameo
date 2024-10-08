import sys
import time
import threading
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

# Args.
args = [sys.argv[2]]

app = server.start(applicationName, args)
print("Started application", applicationName)

requesters = []

N = 5
for id in range(N):
    
    print("Creating requester")
    
    requester = cameopy.coms.Requester.create(app, "responder")
    requester.init()
    requesters.append(requester)
        
    print("Created requester", requester)


tds = []

def requesterProcess(id):
    
    for i in range(numberOfTimes):
        # Send a simple message.
        requesters[id].send(str(i))
        
        response = requesters[id].receiveString()
        print(id, "receives", response)
        

for id in range(N):
    t = threading.Thread(target=requesterProcess, args=(id,))
    t.start()
    tds.append(t)

for id in range(N):
    tds[id].join()
    requesters[id].terminate()

# Stop the responder application.
app.stop()

# Wait for the application.
app.waitFor()

server.terminate()
this.terminate()

print("Finished the application")


    