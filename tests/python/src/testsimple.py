import sys
import cameopy

this = cameopy.This
this.init(sys.argv)

numberOfTimes = 1 if len(sys.argv) < 3 else int(sys.argv[1])

server = this.getServer()

for i in range(numberOfTimes):    
    app = server.start("simplepy")
    state = app.waitFor()
    print("Finished the application", app.getNameId(), "with state", cameopy.toString(state), "and code", app.getExitCode())
