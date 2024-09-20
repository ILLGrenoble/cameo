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

useProxy = False if len(sys.argv) < 5 else (sys.argv[3] == "true") 

options = 0
endpoint = "tcp://localhost:11000";
if useProxy:
    options |= cameopy.option.USE_PROXY
    endpoint = "tcp://localhost:12000";

server = cameopy.Server.create(endpoint, options)
server.init()

print("Responder application is", applicationName)

for i in range(numberOfTimes):
    
    args = ["true" if useProxy else "false"]
    app = server.start(applicationName, args)
    print("Started application", applicationName)

    requester = cameopy.coms.Requester.create(app, "responder")
    requester.setCheckApp(True)
    requester.init()
    
    print("Created requester", requester)
    
    # Send a simple message.
    requester.send("request")
    
    response = requester.receiveString()
    print("Response is", response)
    response = requester.receiveString()
    print("Response 2 is", response)
    
    # Send a two-parts message.
    requester.sendTwoParts("first", "second")
    
    response = requester.receiveString()
    print("Response is", response)
    
    # Send a simple message but do not receive the response immediately.
    requester.send("request");
    
    print("Wait so that the responder has replied")
    time.sleep(1)
    
    response = requester.receiveString()
    print("Response is", response)
    
    # Send a new simple message.
    requester.send("request after wait")
    requester.setTimeout(200)

    response = requester.receiveString()
    
    if not response is None:
        print("Response is", response);
    elif requester.hasTimedout():
        print("Timeout")    
    else:
        print("No response")    

    # The requester needs to resync after a timeout.
    # If the server does not respond within the configured timeout, an error occurs.
    requester.send("request after timeout")
    if requester.hasTimedout():
        print("Timeout while resyncing")
    
    print("Wait so that the server is able to respond")            
    time.sleep(1)
        
    # Resend the request.
    requester.send("request after timeout")
    if not requester.hasTimedout():
        print("No timeout while sending")
    
    response = requester.receiveString();
    print("Response is", response)
    
    # Wait for the application.
    app.waitFor()
    
    requester.terminate()
    
    print("Finished the application")


    