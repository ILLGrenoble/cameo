import sys
import cameopy
import time

this = cameopy.This
this.init(sys.argv)

numberOfTimes = 1 if len(sys.argv) < 3 else int(sys.argv[1])

server = this.getServer()

for i in range(numberOfTimes):
    
    app = server.start("resultpy");
  
    result = app.getResult()
      
    # The variable result can be None.
    if result:
        print("Result", result)
    else:
        print("No result")
        
    print("Finished the application")
