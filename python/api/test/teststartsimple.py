import sys

numberOfTimes = 1 if len(sys.argv) <3 else sys.argv[1]
print("Number of times: ", numberOfTimes)

import cameopy
t= cameopy.This

t.init( sys.argv);

server = t.getServer();
for i  in range(numberOfTimes):	
  app = server.start("simplecpp");
  state = app.waitFor();
  print( "finished the application ", app.getName,
         " with state ", toString(state),
         " and code ", app.getExitCode()
  )
