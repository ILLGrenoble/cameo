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

server = this.getServer()

for i in range(numberOfTimes):
    
    app = server.start(applicationName)
    
    print("Started application", applicationName)
    
    subscriber = cameopy.Subscriber.create(app, "publisher")
    
    while True:
        data = subscriber.receive()
        if data:
            print("Received", data)
        else:
            break
        
    print("Finished stream")
    
    state = app.waitFor()
    
    print("Publisher application terminated")


    