import sys
import time
import cameopy

this = cameopy.This
this.init(sys.argv)

applicationName = ""
numberOfTimes = 1 

if len(sys.argv) >= 3:
    applicationName = sys.argv[1]
    
if len(sys.argv) >= 4:
    numberOfTimes = int(sys.argv[2])

print("Subscriber application is", applicationName)

server = this.getServer()

for i in range(numberOfTimes):
    
    for j in range(5):    
        # Set the name of this application in args
        args = [this.getName()]
        app = server.start(applicationName, args)
    
        print("Started application", applicationName)
        
    
    # Set the publisher to None deletes the previous one and ensures that it is correctly removed.
    publisher = None 
    publisher = cameopy.Publisher.create("publisher")
    
    for k in range(20):
        data = "{" + str(k) + ", " + str(k * k) + "}"
        publisher.send(data)
    
        print("Sent", data)
        
        time.sleep(0.1)
        
    publisher.sendEnd()
    
    apps = server.connectAll(applicationName, 0)
    
    for app in apps:
        app.waitFor()
        print("Finished the application", app.getNameId())
        

print("Finished the application")


    