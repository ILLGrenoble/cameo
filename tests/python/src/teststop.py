import sys
import cameopy
import time

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
    
    # Start the cpp app because handlers not supported in Python.
    app = server.start("stoppy");
  
    print("Waiting...")
    time.sleep(0.1)
    print("Stopping application", app.getNameId())
    app.stop()
      
    result = app.getStringResult()
      
    # The variable result can be None.
    if result:
        print("Result", result)
    else:
        print("No result")
        
    print("Finished the application")
