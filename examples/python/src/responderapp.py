import sys
import cameopy

# Initialize cameo.
this = cameopy.This
this.init(sys.argv)

# Define the stop handler to properly stop.
def stop():
    cameopy.This.cancelAll()

this.handleStop(stop)

# Create the responder.
responder = cameopy.coms.basic.Responder.create("the-responder")
responder.init()
print("Created and initialized responder", responder.getName())

# Set the state.
this.setRunning()

while True:
    
    # Receive the simple request.
    request = responder.receive();
    if request:
        requestStr = request.getString()
        print("Received request", requestStr)
        response = "done"
        
        # Reply.
        request.reply(response)
    else:
        print("Responder is canceled")
        break

# Do not forget to terminate This and the responder.
responder.terminate()
this.terminate()

print("Finished the application")
  
