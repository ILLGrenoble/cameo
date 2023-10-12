import sys
import cameopy

this = cameopy.This
this.init(sys.argv)

numberOfTimes = 1 if len(sys.argv) < 3 else int(sys.argv[1])
useProxy = False if len(sys.argv) < 4 else (sys.argv[2] == "true") 

options = 0
endpoint = "tcp://localhost:11000";
if useProxy:
    options |= cameopy.USE_PROXY
    endpoint = "tcp://localhost:10000";

server = cameopy.Server.create(endpoint, options)
server.init()

for i in range(numberOfTimes):    
    app = server.start("errorpy")
    state = app.waitFor()
    
    if state == cameopy.FAILURE:
        print("Error")
    else:
        print("No error")
    
    print("Finished the application", app, "with state", cameopy.toString(state), "and code", app.getExitCode())
