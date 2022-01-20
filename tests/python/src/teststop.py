import sys
import cameopy
import time

this = cameopy.This
this.init(sys.argv)

numberOfTimes = 1 if len(sys.argv) < 3 else int(sys.argv[1])

server = this.getServer()

for i in range(numberOfTimes):
    
    # Start the cpp app because handlers not supported in Python.
    app = server.start("stoppy");
  
    print("Waiting 100ms...")
    time.sleep(0.1)
    print("Stopping application", app.getNameId())
    app.stop()
      
    result = app.getResult()
      
    # The variable result can be None.
    if result:
        print("Result", result)
    else:
        print("No result")
        
    print("Finished the application")
