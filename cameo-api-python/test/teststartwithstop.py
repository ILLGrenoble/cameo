import sys
import time
numberOfTimes = 1 if len(sys.argv) <3 else sys.argv[1]
print("Number of times: ", numberOfTimes)

import cameopy
t= cameopy.This

t.init( sys.argv);

server = t.getServer();
if t.isAvailable() and  server.isAvailable():
      print( "connected")

for i  in range(numberOfTimes):	
    stopApplication = server.start("stopcpp");
    print("waiting 100ms...")
    time.sleep(0.1)
    print( "stopping application ", stopApplication.getNameId())
    stopApplication.stop()
    result = ""
    if stopApplication.getResult(result):
        print( "stop application returned ", result)
    else:
        print( "stop application has no result" )

    print("finished the application ", stopApplication)
    
