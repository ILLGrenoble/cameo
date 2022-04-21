import sys
import cameopy

this = cameopy.This
this.init(sys.argv)

numberOfTimes = 1 if len(sys.argv) < 3 else int(sys.argv[1])
useProxy = False if len(sys.argv) < 4 else (sys.argv[2] == "true") 

endpoint = "tcp://localhost:11000";
if useProxy:
    endpoint = "tcp://localhost:10000";

server = cameopy.Server.create(endpoint, useProxy)
server.init()

for i in range(numberOfTimes):    
    app = server.start("veryfastpy")
    state = app.waitFor()
    print("Finished the application", app, "with state", cameopy.toString(state), "and code", app.getExitCode())
