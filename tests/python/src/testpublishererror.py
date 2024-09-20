import sys
import cameopy

this = cameopy.This
this.init(sys.argv)

applicationName = ""
numberOfTimes = 1 

if len(sys.argv) >= 3:
    applicationName = sys.argv[1]
    
if len(sys.argv) >= 4:
    numberOfTimes = int(sys.argv[2])

print("Publisher application is", applicationName)

useProxy = False if len(sys.argv) < 5 else (sys.argv[3] == "true") 

options = 0
endpoint = "tcp://localhost:11000";
if useProxy:
    options |= cameopy.option.USE_PROXY
    endpoint = "tcp://localhost:12000";

server = cameopy.Server.create(endpoint, options)
server.init()

for i in range(numberOfTimes):
    
    app = server.start(applicationName)
    
    print("Started application", applicationName)
    
    subscriber = cameopy.coms.Subscriber.create(app, "publisher")
    subscriber.setCheckApp(True)
    subscriber.init()
    
    print("Created subscriber", subscriber)
    
    while True:
        data = subscriber.receiveString()
        if data:
            print("Received", data)
        else:
            break
        
    print("Finished stream")
    
    state = app.waitFor()
    
    print("Publisher application terminated with state", cameopy.toString(state))


    